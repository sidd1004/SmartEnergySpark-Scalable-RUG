package spark.utility

import org.apache.spark.sql.{DataFrame, SparkSession}
import spark.utility.Constants.Kafka.KAFKA_URL

object SparkReaders {

  object DataFrameReaders {

    def csv(sparkSession: SparkSession, inputPath: String, options: Map[String, String]): DataFrame = {
      sparkSession.read.options(options).csv(inputPath)
    }

    def json(sparkSession: SparkSession, inputPath: String, options: Map[String, String]): DataFrame = {
      sparkSession.read.options(options).json(inputPath)
    }

    def text(sparkSession: SparkSession, inputPath: String, options: Map[String, String]): DataFrame = {
      sparkSession.read.options(options).text(inputPath)
    }


    def kafkBatchQuery(sparkSession: SparkSession, options: Map[String, String]): DataFrame = {
      sparkSession
        .read
        .format("kafka")
        .options(options)
        .option("startingOffsets", "earliest")
        .option("endingOffsets", "latest")
        .load()
    }

    def kafkStreamQuery(sparkSession: SparkSession, options: Map[String, String]): DataFrame = {
      sparkSession
        .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "my-cluster-kafka-bootstrap:9092")
        .option("subscribe", "StreamPipeline")
        .option("startingOffsets", "latest")
        // .options(options)
        .load()
    }
  }
}
