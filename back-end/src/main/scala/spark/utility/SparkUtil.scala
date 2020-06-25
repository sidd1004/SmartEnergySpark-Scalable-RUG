package spark.utility

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.StructField

import scala.collection.mutable

object SparkUtil {

  def flattenStructure(dataFrame: DataFrame, columnName: String, fields: Array[StructField]): DataFrame = {
    fields.foldLeft(dataFrame) {
      (df, columns) => df.withColumn(columns.name, col(s"$columnName.${columns.name}"))
    }
  }

  val euclidianDist = udf((vector1: mutable.WrappedArray[String], vector2: mutable.WrappedArray[String]) => {
    val v1 = Vectors.dense(vector1.map(_.toDouble).toArray)
    val v2 = Vectors.dense(vector2.map(_.toDouble).toArray)
    math.sqrt(Vectors.sqdist(v1, v2))
  })

}
