package spark.appliancelc

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.functions.{from_json, lit, _}
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import spark.orchestrator.Model
import spark.utility.Constants.Cassandra._
import spark.utility.Constants.Kafka._
import spark.utility.Constants._
import spark.utility.cassandra.CassandraStartup
import spark.utility.{Delimiters, SparkReaders, SparkUtil}

import scala.collection.JavaConverters._

object PredictLifeCycleStream extends SparkSessionBuilder {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf")
    val cassandraHosts: Seq[String] = config.getString(CASSANDRA_HOTS).split(Delimiters.COMMA)
    val cassandraPort: Int = config.getString(CASSANDRA_PORT).toInt
    val cassandraKeyspace: String = config.getString(CASSANDRA_KEYSPACE)
    val cassandraReplication: Int = config.getInt(CASSANDRA_REPLICATION_FACTOR)
    val cassandraTableName: String = config.getString(CASSANDRA_TABLE_NAME)
    val algorithmType = config.getString(ALGORITHM_TYPE)

    val spark = buildSparkSession

    val session = CassandraStartup.init(cassandraHosts,
                                        cassandraPort,
                                        cassandraKeyspace,
                                        "execution_logs",
                                        cassandraReplication)

    val start_train_time = DateTime.now().getMillis

    val trainingData = spark.read.cassandraFormat(cassandraTableName, cassandraKeyspace).load()
    val model = Model(algorithmType, trainingData, k = 15, isStreaming = true, config.getInt(MAXIMUM_ITERATIONS))

    val end_training_time = DateTime.now().getMillis
    val trainingTime = (end_training_time - start_train_time) / 1000

    session.execute(s"insert into $cassandraKeyspace.execution_logs(timestamp, stage, exec_time) values('${end_training_time.toString}', '${algorithmType}_training_stream', ${trainingTime})")

    val options = Map(
      KAFKA_BOOTSTRAP_SERVERS -> config.getString(KAFKA_URL),
      SUBSCRIBE_TOPIC -> config.getString(STREAM_QUERY_TOPIC)
      )

    while (true) {

      val df = SparkReaders.DataFrameReaders.kafkStreamQuery(spark, options)
                           .selectExpr(deserializeKeyToString, deserializeValueToString)
                           .drop(KEY)

      val start_prediction_time = DateTime.now().getMillis

      val schema = lit("struct<applianceage:bigint,applianceid:string,energy:string,uniqueid:string>")
      val df2 = df.withColumn(JSON_DATA, from_json(col(VALUE), schema, Map[String, String]().asJava))
      val columnsStructData = df2.select(JSON_DATA).schema.fields.head.dataType.asInstanceOf[StructType].fields

      // @formatter:off
    val testData = SparkUtil.flattenStructure(df2, JSON_DATA, columnsStructData)
      .drop(VALUE, JSON_DATA)
    // @formatter:on

      val res = model.predict(testData)

      val sink = res
        .writeStream
        .queryName("KafkaToCassandraForeach")
        .outputMode("update")
        .foreach(new CassandraSinkForeach(algorithmType))
        .start().awaitTermination(1000)

      val end_prediction_time = DateTime.now().getMillis
      val predictionTime = (end_prediction_time - start_prediction_time) / 1000
      session.execute(s"insert into $cassandraKeyspace.execution_logs(timestamp, stage, exec_time) values('${end_prediction_time.toString}', '${algorithmType}_testing_stream', ${predictionTime})")

      println("####################### WAITING FOR MORE DATA ####################### ")
      Thread.sleep(30000)
    }
  }
}
