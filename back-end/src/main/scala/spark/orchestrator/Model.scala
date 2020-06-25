package spark.orchestrator

import org.apache.spark.sql.DataFrame
import spark.kmeans.SparkKMeans
import spark.knn.SparkKnn
import spark.utility.Algorithm

case class Model(algorithm: String,
                 trainingDataFrame: DataFrame,
                 k: Int = 1,
                 isStreaming: Boolean = false,
                 maxItr: Int = 1) {
  private val trainedDF = if (algorithm == Algorithm.kmeans) {
    /**
     * Kmeans is optimized to support the application and the K value is derived in the training phase
     * K is only applicable for KNN
     */
    if (isStreaming) {
      SparkKMeans.trainStream(trainingDataFrame, maxItr).cache()

    } else {
      SparkKMeans.train(trainingDataFrame, maxItr).cache()
    }
  } else {
    SparkKnn.train(trainingDataFrame)
  }

  def predict(dataFrame: DataFrame): DataFrame = {
    if (algorithm.equalsIgnoreCase(Algorithm.kmeans))
      SparkKMeans.predict(trainedDF, dataFrame)
    else if (algorithm == Algorithm.knn && isStreaming) {
      SparkKnn.classifyPointStream(trainedDF, dataFrame, k)
    }
    else
      SparkKnn.classifyPoint(trainedDF, dataFrame, k)
  }
}