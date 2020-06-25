package spark.example

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object test {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("InputDataMod2").master("local").getOrCreate()
    import spark.implicits._
    spark.sparkContext.setLogLevel("ERROR")
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
    val rand = new scala.util.Random(100)
    val warrantyMap = Map("Dishwasher" -> 2,
      "Furnace" -> 4,
      "Fridge" -> 3,
      "WineCellar" -> 3,
      "Microwave" -> 3,
      "GarageDoor" -> 5
    )
    val homeSensorData = spark.read.option("header", "true").csv("src/main/resources/output/*").show(false)
  }


}
