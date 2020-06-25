package spark.preprocess

import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.functions.{col, _}
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.{DataFrame, Dataset, Row, SaveMode, SparkSession}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Period}
import spark.utility.Constants.Cassandra._
import spark.utility.{Delimiters, SparkReaders}
import spark.utility.cassandra.CassandraStartup
import spark.utility.Constants.{ENERGY, _}
import spark.utility.Constants.Appliances._

import scala.util.Random

object PublishTrainData {
  def main(args: Array[String]): Unit = {


    val configuration = ConfigFactory.load(APPLICATION_CONF)
    // read the configuration for Cassandra
    val cassandraHosts: Seq[String] = configuration.getString(CASSANDRA_HOTS).split(Delimiters.COMMA)
    val cassandraPort: Int = configuration.getString(CASSANDRA_PORT).toInt
    val cassandraKeyspace: String = configuration.getString(CASSANDRA_KEYSPACE)
    val cassandraReplication: Int = configuration.getInt(CASSANDRA_REPLICATION_FACTOR)
    val cassandraTableName: String = configuration.getString(CASSANDRA_TABLE_NAME)
    CassandraStartup.init(cassandraHosts, cassandraPort, cassandraKeyspace, cassandraTableName, cassandraReplication)
    CassandraStartup.init(cassandraHosts, cassandraPort, cassandraKeyspace, "mst_appliance", cassandraReplication)
    CassandraStartup.init(cassandraHosts,
                          cassandraPort,
                          cassandraKeyspace,
                          "prediction_knn",
                          cassandraReplication)

    CassandraStartup.init(cassandraHosts,
                          cassandraPort,
                          cassandraKeyspace,
                          "prediction_kmeans",
                          cassandraReplication)

    CassandraStartup.init(cassandraHosts,
                          cassandraPort,
                          cassandraKeyspace,
                          "execution_logs",
                          cassandraReplication)

    val spark = SparkSession.builder().appName("DataPreProcessor")
                            .config(SPARK_CASSANDRA_HOST, cassandraHosts.head)
                            .config(SPARK_CASSANDRA_PORT, cassandraPort)
                            .master(configuration.getString(SPARK_MASTER)).getOrCreate()


    import spark.implicits._
    spark.sparkContext.setLogLevel("ERROR")
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val rand = new scala.util.Random(100)
    val warrantyMap = Map(DISH_WASHER -> 2, FURNACE -> 4, FRIDGE -> 3, WINE_CELLAR -> 3, MICROWAVE -> 3, GARAGE_DOOR -> 5)

    val homeSensorData = SparkReaders.DataFrameReaders
                                     .csv(spark, "/opt/spark/input/Home_data.csv", Map(CSV.HEADER -> TRUE))
                                     .drop(WARRANTY_DATE).as[RawData]

    val appliances_discretized = List(DISH_WASHER, FURNACE, FRIDGE, WINE_CELLAR, MICROWAVE, GARAGE_DOOR)

    val discreitzeAppliance = udf((appliance: String) => appliances_discretized.indexOf(appliance))

    val trainingDF2 = homeSensorData.withColumn(APPLIANCE_AGE, months_between(date_format(col(CAPTURE_DATE), DateFormats.YYYY_MM_DD),
                                                                              date_format(col(PURCHASE_DATE), DateFormats.YYYY_MM_DD)).cast(IntegerType))
                                    .withColumn(APPLIANCE_ID, discreitzeAppliance(col(APPLIANCE)))
                                    .withColumn(UNIQUE_ID, concat_ws("-", unix_timestamp().cast("long"), monotonically_increasing_id() + 1))
                                    .selectExpr(APPLIANCE_AGE, ENERGY, UNIQUE_ID, APPLIANCE_ID)
                                    .as[InputData2]
                                    .transform(addVariation(_, 0.2, spark))

    val splitDataFrame = trainingDF2.randomSplit(Array(0.95, 0.05), seed = 100L)
    val trainingDF = splitDataFrame(0)
    val testingDFtmp = splitDataFrame(1)
    val testingDF_outlasted = testingDFtmp.transform(addVariation(_, 0.4, "+", spark))
    //    // val testing_norm = trainingDF.sample(0.01)
    //    val testing_predict = testingDFtmp.sample(0.5).transform(addVariation(_, 0.4, "-", spark))
    //
    //    val testingDF = testingDF_outlasted.union((testing_predict))

    val testingDF = SparkReaders.DataFrameReaders
                                .csv(spark, "/opt/spark/input/testing_data.csv", Map(CSV.HEADER -> TRUE))
                                .withColumn(CAPTURE_DATE, current_date())
                                .withColumn(APPLIANCE_AGE, months_between(date_format(col(CAPTURE_DATE), DateFormats.YYYY_MM_DD),
                                                                          date_format(col(PURCHASE_DATE), DateFormats.YYYY_MM_DD)).cast(IntegerType))
                                .withColumn(APPLIANCE_ID, discreitzeAppliance(col(APPLIANCE)))
                                .withColumn(UNIQUE_ID, concat_ws("-", unix_timestamp().cast("long"), monotonically_increasing_id() + 1))
                                .selectExpr(APPLIANCE_AGE, ENERGY, UNIQUE_ID, APPLIANCE_ID)
                                .as[InputData2]
                                .transform(addVariation(_, 0.3, spark)).union(testingDF_outlasted)

    // val splitDataFrame = res.randomSplit(Array(0.8, 0.2), seed = 100L)
    trainingDF.drop(FILE_NAME).write.cassandraFormat(cassandraTableName, cassandraKeyspace).mode(SaveMode.Append).save()
    println("Dumped to Cassandra")
    //    trainingDF.coalesce(1).write.option(CSV.HEADER, "true").json("src/main/resources/output/training/")
    //    testingDF.coalesce(1).write.option(CSV.HEADER, "true").json("src/main/resources/output/testing/")
    //    var sample = 57
    //    while (sample != 0) {
    //      val timestamp = DateTime.now().getMillis
    //      val df = spark.read.json("src/main/resources/output/testing/").sample(0.01)
    //      df.write.json(s"src/main/resources/streamoutputs/$timestamp/")
    //      sample = sample - 1
    //    }
    spark.stop()
  }

  def addVariation(dataframe: Dataset[InputData2], threshold: Double, variationType: String, session: SparkSession): Dataset[InputData2] = {
    import session.implicits._
    dataframe.map(
      row => {
        var variation = Random.nextDouble()
        val applianceAge = row.applianceage

        while (variation >= threshold || variation <= 0.1)
          variation = Random.nextDouble()

        if (variationType.equalsIgnoreCase("+"))
          InputData2((applianceAge + (applianceAge * variation)).toInt, row.energy, row.uniqueid, row.applianceid)
        else
          InputData2((applianceAge - (applianceAge * variation)).toInt, row.energy, row.uniqueid, row.applianceid)
      })
  }

  def addVariation(dataframe: Dataset[InputData2], threshold: Double, sparkSession: SparkSession): Dataset[InputData2] = {
    import sparkSession.implicits._
    dataframe.map(
      row => {
        val variation = Random.nextDouble()

        val applianceAge = row.applianceage
        if (variation <= threshold)
          InputData2((applianceAge - applianceAge * variation).toInt, row.energy, row.uniqueid, row.applianceid)

        else if (variation >= 1 - threshold)
          InputData2((applianceAge + applianceAge * (1 - variation)).toInt, row.energy, row.uniqueid, row.applianceid)

        else
          InputData2((applianceAge - applianceAge * 0.2).toInt, row.energy, row.uniqueid, row.applianceid)
      })
  }
}

case class RawData(purchase_date: String, appliance: String, capture_date: String, energy: String)

case class InputData2(applianceage: Int, energy: String, uniqueid: String, applianceid: String)

case class InputData(purchase_date: String, appliance: String, capture_date: String, energy: String, labels: Int = -1)
