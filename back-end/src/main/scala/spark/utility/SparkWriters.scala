package spark.utility

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkWriters {

  object DataFrameWriters {
    def kafkaStreamWriter(dataFrame: DataFrame, options: Map[String, String], awaitTermination: Long): Unit = {
      val query = dataFrame
        .writeStream
        .format("kafka")
        .options(options)
        .start()
      query.awaitTermination(awaitTermination)
      query.stop()
    }

    def kafkaBatchWriter(dataFrame: DataFrame, options: Map[String, String]): Unit = {
      dataFrame
        .write
        .format("kafka")
        .options(options)
        .save()
    }

  }

}
