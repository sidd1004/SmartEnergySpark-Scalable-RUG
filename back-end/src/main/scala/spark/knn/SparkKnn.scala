package spark.knn

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{abs, array, asc, col, concat, first, lit, mean, round, row_number, udf, when, _}
import spark.utility.Constants._

import scala.collection.mutable
import spark.utility.SparkUtil

object SparkKnn {

  def train(dataFrame: DataFrame): DataFrame = {
    dataFrame.withColumn(FEATURE_VECTOR_TRAINING, array(col(APPLIANCE_AGE), col(ENERGY)))
             .drop(UNIQUE_ID)
             .cache()
  }

  def classifyPoint(model: DataFrame, testData: DataFrame, k: Int = 1): DataFrame = {

    val predictedDF = testData.withColumn(FEATURE_VECTOR_TO_PREDICT, array(col(APPLIANCE_AGE), col(ENERGY)))
                              .withColumnRenamed(APPLIANCE_AGE, APPLIANCE_AGE_TEST)
                              .join(model.drop(ENERGY), List(APPLIANCE_ID), LEFT_JOIN)
                              .dropDuplicates()
                              .withColumn(DISTANCE, SparkUtil.euclidianDist(col(FEATURE_VECTOR_TRAINING), col(FEATURE_VECTOR_TO_PREDICT)))
                              .withColumn(ROW_NUMBER, row_number().over(Window.partitionBy(UNIQUE_ID).orderBy(asc(DISTANCE))))
                              .filter(col(ROW_NUMBER) <= k)
                              .groupBy(UNIQUE_ID, APPLIANCE_AGE_TEST)
                              .agg(mean(APPLIANCE_AGE).as(MEAN_APPLIANCE_AGE), first(APPLIANCE_AGE).as(APPLIANCE_AGE), first(APPLIANCE_ID).as(APPLIANCE_ID), first(ENERGY).as(ENERGY))
                              .withColumn(PREDICTED_AGE, col(MEAN_APPLIANCE_AGE) - col(APPLIANCE_AGE_TEST))
                              .withColumn(COMMENTS, when(col(PREDICTED_AGE) <= 0.0, concat(lit("Outlasted at current age :"), abs(col(PREDICTED_AGE)), lit(" Months")))
                                .otherwise(concat(lit("Can survive for another :"), abs(round(col(PREDICTED_AGE))), lit(" Months")))).drop(MEAN_APPLIANCE_AGE)
                              .withColumn(IS_CONTINUOUS_TRAINING, when(col(PREDICTED_AGE) < 0.0, true).otherwise(false))
                              .withColumn(PREDICTED_AGE, when(col(PREDICTED_AGE) <= 0.0, col(APPLIANCE_AGE_TEST)).otherwise(col(APPLIANCE_AGE)))
    predictedDF
  }

  def classifyPointStream(model: DataFrame, testData: DataFrame, k: Int = 1): DataFrame = {

    val predictedDF = testData.withColumn(FEATURE_VECTOR_TO_PREDICT, array(col(APPLIANCE_AGE), col(ENERGY)))
                              .withColumnRenamed(APPLIANCE_AGE, APPLIANCE_AGE_TEST)
                              .join(model.drop(ENERGY), List(APPLIANCE_ID), LEFT_JOIN)
                              .withColumn(DISTANCE, SparkUtil.euclidianDist(col(FEATURE_VECTOR_TRAINING), col(FEATURE_VECTOR_TO_PREDICT)))
                              .dropDuplicates()

    val getTopK = udf((data: mutable.WrappedArray[String], k: Int) => {
      val KAge = data.map(_.split(":")).sortBy(_ (0)).take(k).map(_ (1).toDouble)
      KAge.sum / KAge.length
    })

    predictedDF.groupBy(UNIQUE_ID, APPLIANCE_AGE_TEST, ENERGY, APPLIANCE_ID)
               .agg(collect_list(concat_ws(":", col(DISTANCE), col(APPLIANCE_AGE))).as("data"))
               .withColumn(MEAN_APPLIANCE_AGE, getTopK(col("data"), lit(k)))
               .withColumn(PREDICTED_AGE, col(MEAN_APPLIANCE_AGE) - col(APPLIANCE_AGE_TEST))
               .withColumn(APPLIANCE_AGE, col(MEAN_APPLIANCE_AGE))
               .withColumn(COMMENTS, when(col(PREDICTED_AGE) <= 0.0, concat(lit("Outlasted at current age : "), abs(col(APPLIANCE_AGE_TEST)), lit(" Months")))
                 .otherwise(concat(lit("Can survive for another :"), abs(col(PREDICTED_AGE)), lit(" Months"))))
               .withColumn(IS_CONTINUOUS_TRAINING, when(col(PREDICTED_AGE) < 0.0, true).otherwise(false))
               .withColumn(PREDICTED_AGE, when(col(PREDICTED_AGE) <= 0.0, col(APPLIANCE_AGE)).otherwise(col(APPLIANCE_AGE_TEST)))
               .select(UNIQUE_ID, APPLIANCE_ID, APPLIANCE_AGE, COMMENTS, ENERGY, PREDICTED_AGE, IS_CONTINUOUS_TRAINING)
  }
}
