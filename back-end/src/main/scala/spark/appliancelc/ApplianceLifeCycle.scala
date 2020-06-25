package spark.appliancelc

import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession, functions}
import spark.utility.Constants.Cassandra._
import spark.utility.Delimiters
import spark.utility.cassandra.CassandraStartup


object ApplianceLifeCycle {
  def main(args: Array[String]): Unit = {
    val configuration = ConfigFactory.load("application.conf")

    // read the configuration for Cassandra
    val cassandraHosts: Seq[String] = configuration.getString(CASSANDRA_HOTS).split(Delimiters.COMMA)
    val cassandraPort: Int = configuration.getString(CASSANDRA_PORT).toInt
    val cassandraKeyspace: String = configuration.getString(CASSANDRA_KEYSPACE)
    val cassandraReplication: Int = configuration.getInt(CASSANDRA_REPLICATION_FACTOR)
    val cassandraTableName: String = configuration.getString(CASSANDRA_TABLE_NAME)
    val session = CassandraStartup.init(cassandraHosts, cassandraPort, cassandraKeyspace, cassandraTableName, cassandraReplication)

    val spark = SparkSession.builder().appName("InputDataMod2")
      .config("spark.cassandra.connection.host", cassandraHosts.head)
      .config("spark.cassandra.connection.port", cassandraPort)
      .master("local").getOrCreate()

    import spark.implicits._
    spark.sparkContext.setLogLevel("ERROR")
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
    val rand = new scala.util.Random(100)

    val inputData = spark.read.option("header", "true").csv(configuration.getString("appliance.training.data")).toDF

    val df2 = inputData.withColumn("id", functions.monotonically_increasing_id())
      .withColumnRenamed("Type", "appliances")
      .withColumn("id", functions.col("id").cast("String"))
      .as[InputDataType].toDF
    df2.printSchema()
   // df2.createCassandraTable(cassandraKeyspace, cassandraTableName)
    df2.write.cassandraFormat(cassandraTableName, cassandraKeyspace).mode(SaveMode.Append).save()

    val trainingData = spark.read.cassandraFormat(cassandraTableName, cassandraKeyspace).load()
    train(trainingData)

    println("DONE")

    def train(df: DataFrame): Unit = {
      println("TODO: IMPLEMENT TRAINING ALGORITHM")
    }
  }
}

case class InputDataType(id: String, purchase_date: String, appliances: String, capture_date: String, energy: String, labels: String)
