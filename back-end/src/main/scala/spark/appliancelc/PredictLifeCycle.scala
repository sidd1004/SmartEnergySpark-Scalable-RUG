package spark.appliancelc

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.joda.time.DateTime
import spark.orchestrator.Model
import spark.utility.Constants.Cassandra._
import spark.utility.Constants.Kafka._
import spark.utility.Constants._
import spark.utility.cassandra.CassandraStartup
import spark.utility.{Delimiters, SparkReaders, SparkUtil}

object PredictLifeCycle {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load(APPLICATION_CONF)
    val cassandraHosts: Seq[String] = config.getString(CASSANDRA_HOTS).split(Delimiters.COMMA)
    val cassandraPort: Int = config.getString(CASSANDRA_PORT).toInt
    val cassandraKeyspace: String = config.getString(CASSANDRA_KEYSPACE)
    val cassandraReplication: Int = config.getInt(CASSANDRA_REPLICATION_FACTOR)
    val cassandraTrainingTableName: String = config.getString(CASSANDRA_TABLE_NAME)

    val algorithmType = config.getString(ALGORITHM_TYPE)
    val tableName = "prediction_" + algorithmType.toLowerCase

    val session = CassandraStartup.init(cassandraHosts,
                                        cassandraPort,
                                        cassandraKeyspace,
                                        "execution_logs",
                                        cassandraReplication)


    val spark = SparkSession.builder().appName("PredictApplianceLifeSpanBatch")
                            .config("spark.sql.crossJoin.enabled", true)
                            .config(SPARK_CASSANDRA_HOST, cassandraHosts.head)
                            .config(SPARK_CASSANDRA_PORT, cassandraPort)
                            .master(config.getString(SPARK_MASTER)).getOrCreate()

    val start_train_time = DateTime.now().getMillis

    val trainingData = spark.read.cassandraFormat(cassandraTrainingTableName, cassandraKeyspace)
                            .load()

    val model = Model(algorithmType, trainingData, 15, isStreaming = false, config.getInt(MAXIMUM_ITERATIONS))

    val end_training_time = DateTime.now().getMillis
    val trainingTime = (end_training_time - start_train_time) / 1000

    session.execute(s"insert into $cassandraKeyspace.execution_logs(timestamp, stage, exec_time) values('${end_training_time.toString}', '${algorithmType}_training', ${trainingTime})")

    while (true) {

      val df = SparkReaders.DataFrameReaders.kafkBatchQuery(spark,
                                                            Map(KAFKA_BOOTSTRAP_SERVERS -> config.getString(KAFKA_URL),
                                                                SUBSCRIBE_TOPIC -> config.getString(BATCH_QUERY_TOPIC))
                                                            )
                           .selectExpr(deserializeKeyToString, deserializeValueToString)
                           .drop(KEY)

      import org.apache.spark.sql.functions.{from_json, lit, schema_of_json}
      import spark.implicits._

      import collection.JavaConverters._

      if (df.head(1).nonEmpty) {
        val start_prediction_time = DateTime.now().getMillis

        val schema = schema_of_json(lit(df.select(col(VALUE)).as[String].first))
        val df2 = df.withColumn(JSON_DATA, from_json(col(VALUE), schema, Map[String, String]().asJava))
        val columnsStructData = df2.select(JSON_DATA).schema.fields.head.dataType.asInstanceOf[StructType].fields

        val testData = SparkUtil.flattenStructure(df2, JSON_DATA, columnsStructData).drop(VALUE, JSON_DATA)


        val res = model.predict(testData)


        res.select(UNIQUE_ID, APPLIANCE_ID, APPLIANCE_AGE, COMMENTS, ENERGY, PREDICTED_AGE)
           .write.cassandraFormat(tableName, cassandraKeyspace)
           .mode(SaveMode.Append)
           .save()

        val end_prediction_time = DateTime.now().getMillis
        val predictionTime = (end_prediction_time - start_prediction_time) / 1000

        session.execute(s"insert into $cassandraKeyspace.execution_logs(timestamp, stage, exec_time) values('${end_prediction_time.toString}', '${algorithmType}_testing', ${predictionTime})")

        if (config.getBoolean(CONTINUOUS_TRAINING)) {
          res.filter(col(IS_CONTINUOUS_TRAINING) === true).drop(IS_CONTINUOUS_TRAINING)
             .withColumn(UNIQUE_ID, concat_ws("-", unix_timestamp().cast("long"), monotonically_increasing_id() + 1))
             .select(UNIQUE_ID, APPLIANCE_ID, APPLIANCE_AGE, ENERGY)
             .write.cassandraFormat(cassandraTrainingTableName, cassandraKeyspace).mode(SaveMode.Append).save()
        }

        println("####################### WAITING FOR MORE DATA ####################### ")
        Thread.sleep(30000)
      } else {
        println("BatchPipeline is empty")
        println("####################### WAITING FOR MORE DATA ####################### ")
        Thread.sleep(30000)
      }
    }

  }
}
