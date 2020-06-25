package spark.kmeans

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import spark.utility.Constants._
import spark.utility.SparkUtil

object SparkKMeans {
  def train(dataFrame: DataFrame, max_itr: Int = 1): DataFrame = {

    val datadf = dataFrame.withColumn(VECTOR, array(col(APPLIANCE_AGE), col(ENERGY)))

    val initalPrototype = datadf.groupBy(APPLIANCE_ID)
                                .agg(first(APPLIANCE_AGE).as(PROTOTYPE_APPLIANCE_AGE), first(ENERGY).as(PROTOTYPE_ENERGY))
                                .select(PROTOTYPE_APPLIANCE_AGE, PROTOTYPE_ENERGY, APPLIANCE_ID)
                                .withColumnRenamed(APPLIANCE_ID, PROTOTYPE_ID)
                                .withColumn(PROTOTYPE_VECTOR, array(col(PROTOTYPE_APPLIANCE_AGE), col(PROTOTYPE_ENERGY)))
    var stable = false
    val model = (0 until max_itr).foldLeft(initalPrototype) {
      (dataframe, itr) =>
        val df = datadf.join(dataframe)
                       .withColumn(DISTANCE, SparkUtil.euclidianDist(col(VECTOR), col(PROTOTYPE_VECTOR)))

        df.withColumn(ROW_NUMBER, row_number().over(Window.partitionBy(UNIQUE_ID).orderBy(asc(DISTANCE))))
          .filter(col(ROW_NUMBER) === 1)
          .withColumn(OLD_PROTOTYPE_VECTOR, col(PROTOTYPE_VECTOR))
          .groupBy(PROTOTYPE_ID)
          .agg(mean(APPLIANCE_AGE).as(PROTOTYPE_APPLIANCE_AGE),
               mean(ENERGY).as(PROTOTYPE_ENERGY),
               first(OLD_PROTOTYPE_VECTOR).as(OLD_PROTOTYPE_VECTOR))
          .withColumn(PROTOTYPE_VECTOR, array(col(PROTOTYPE_APPLIANCE_AGE), col(PROTOTYPE_ENERGY)))
          .withColumn(VARIATION, SparkUtil.euclidianDist(col(OLD_PROTOTYPE_VECTOR), col(PROTOTYPE_VECTOR)))
          .withColumn(VARIATION, ceil(lit(100) * col(VARIATION)) / 100)
          .select(PROTOTYPE_ID, PROTOTYPE_APPLIANCE_AGE, PROTOTYPE_ENERGY, PROTOTYPE_VECTOR)
    }

    model
  }

  def trainStream(dataFrame: DataFrame, max_itr: Int = 1): DataFrame = {

    val datadf = dataFrame.withColumn(VECTOR, array(col(APPLIANCE_AGE), col(ENERGY)))

    val initalPrototype = datadf.groupBy(APPLIANCE_ID)
                                .agg(first(APPLIANCE_AGE).as(PROTOTYPE_APPLIANCE_AGE), first(ENERGY).as(PROTOTYPE_ENERGY))
                                .select(PROTOTYPE_APPLIANCE_AGE, PROTOTYPE_ENERGY, APPLIANCE_ID)
                                .withColumnRenamed(APPLIANCE_ID, PROTOTYPE_ID)
                                .withColumn(PROTOTYPE_VECTOR, array(col(PROTOTYPE_APPLIANCE_AGE), col(PROTOTYPE_ENERGY)))
    var stable = false
    val model = (0 until max_itr).foldLeft(initalPrototype) {
      (dataframe, itr) =>
        val df = datadf.join(dataframe)
                       .withColumn(DISTANCE, SparkUtil.euclidianDist(col(VECTOR), col(PROTOTYPE_VECTOR)))

        df.groupBy(UNIQUE_ID).agg(min(DISTANCE).as(MIN_DIST)).join(df, UNIQUE_ID)
          .filter(col(DISTANCE) === col(MIN_DIST))
          .withColumn(OLD_PROTOTYPE_VECTOR, col(PROTOTYPE_VECTOR))
          .groupBy(PROTOTYPE_ID)
          .agg(mean(APPLIANCE_AGE).as(PROTOTYPE_APPLIANCE_AGE),
               mean(ENERGY).as(PROTOTYPE_ENERGY),
               first(OLD_PROTOTYPE_VECTOR).as(OLD_PROTOTYPE_VECTOR))
          .withColumn(PROTOTYPE_VECTOR, array(col(PROTOTYPE_APPLIANCE_AGE), col(PROTOTYPE_ENERGY)))
          .withColumn(VARIATION, SparkUtil.euclidianDist(col(OLD_PROTOTYPE_VECTOR), col(PROTOTYPE_VECTOR)))
          .withColumn(VARIATION, ceil(lit(100) * col(VARIATION)) / 100)
          .select(PROTOTYPE_ID, PROTOTYPE_APPLIANCE_AGE, PROTOTYPE_ENERGY, PROTOTYPE_VECTOR)
    }

    model
  }

  def predict(model: DataFrame, data: DataFrame): DataFrame = {
    val test = data.withColumn(VECTOR, array(col(APPLIANCE_AGE), col(ENERGY)))
    val df = test.join(model).withColumn(DISTANCE, SparkUtil.euclidianDist(col(VECTOR), col(PROTOTYPE_VECTOR)))
    df.withColumn(ROW_NUMBER, row_number().over(Window.partitionBy(UNIQUE_ID).orderBy(asc(DISTANCE))))
      .filter(col(ROW_NUMBER) === 1)
      .select(UNIQUE_ID, APPLIANCE_AGE, APPLIANCE_ID, ENERGY, PROTOTYPE_APPLIANCE_AGE)
      .withColumn(PREDICTED_AGE, col(PROTOTYPE_APPLIANCE_AGE) - col(APPLIANCE_AGE))
      .withColumn(COMMENTS, when(col(PREDICTED_AGE) <= 0.0, concat(lit("Outlasted at current age :"), abs(col(APPLIANCE_AGE)), lit(" Months")))
        .otherwise(concat(lit("Can survive for another :"), abs(round(col(PREDICTED_AGE))), lit(" Months"))))
      .withColumn(IS_CONTINUOUS_TRAINING, when(col(PREDICTED_AGE) < 0.0, true).otherwise(false))
      .withColumn(PREDICTED_AGE, when(col(PREDICTED_AGE) <= 0.0, ceil(col(APPLIANCE_AGE))).otherwise(ceil(col(PROTOTYPE_APPLIANCE_AGE))))
      .drop(PROTOTYPE_APPLIANCE_AGE)
  }
}
