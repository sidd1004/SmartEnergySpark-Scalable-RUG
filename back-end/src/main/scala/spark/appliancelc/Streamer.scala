package spark.appliancelc

import java.io.File
import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.streams.StreamsConfig
import org.apache.spark.sql.functions.{concat_ws, monotonically_increasing_id, unix_timestamp}
import org.apache.spark.sql.{SparkSession, functions}
import org.joda.time.DateTime
import spark.utility.Constants.CSV
import spark.utility.Constants.Kafka._
import spark.utility.Constants._
import spark.utility.{SparkReaders, SparkWriters}

object Streamer {

  def main(args: Array[String]): Unit = {
    val configuration = ConfigFactory.load(APPLICATION_CONF)
    val spark = SparkSession.builder().appName("InputStreamer").master(configuration.getString(SPARK_MASTER)).getOrCreate()
    val files = getListOfFiles("/opt/spark/streaminput/")
    val r = scala.util.Random
    val jsonData = SparkReaders.DataFrameReaders.text(spark, configuration.getString(TEST_DATA_PATH), Map())
    var counter = 0
    while (true) {
      val timestamp = DateTime.now().getMillis
      val path = files(r.nextInt(56))
      val streamData = spark.readStream.text(path + "/")
      val streamOptions = Map(
        KAFKA_BOOTSTRAP_SERVERS -> configuration.getString(KAFKA_URL),
        // STARTING_OFFSETS -> configuration.getString(STREAM_QUERY_STARTING_OFFSET),
        TOPIC -> configuration.getString(STREAM_QUERY_TOPIC),
        CHECKPOINT_LOCATION -> configuration.getString(STREAM_QUERY_CHECKPOINT)
        )

      SparkWriters.DataFrameWriters.kafkaStreamWriter(streamData, streamOptions, 10000)
      println("Stream Query successful")
      counter += 1
      if (counter == 1 || counter == 13) {
        val batchOptions = Map(
          KAFKA_BOOTSTRAP_SERVERS -> configuration.getString(KAFKA_URL),
          TOPIC -> configuration.getString(BATCH_QUERY_TOPIC)
          )
        val batchData = jsonData.sample(0.05).withColumn(UNIQUE_ID, concat_ws("-", unix_timestamp().cast("long"),
                                                                             monotonically_increasing_id() + 1))

        SparkWriters.DataFrameWriters.kafkaBatchWriter(batchData, batchOptions)
        println("Batch Query successful")
        counter = 1
      }
    }
  }

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isDirectory).toList
    } else {
      List[File]()
    }
  }

}
