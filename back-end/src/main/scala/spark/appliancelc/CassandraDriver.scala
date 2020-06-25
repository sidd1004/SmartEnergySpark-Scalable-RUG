package spark.appliancelc

import com.datastax.spark.connector.cql.CassandraConnector

class CassandraDriver extends SparkSessionBuilder {
  // This object will be used in CassandraSinkForeach to connect to Cassandra DB from an executor.
  // It extends SparkSessionBuilder so to use the same SparkSession on each node.
  val spark = buildSparkSession
  import spark.implicits._
  val connector = CassandraConnector(spark.sparkContext.getConf)
  // Define Cassandra's table which will be used as a sink
  /* For this app I used the following table:
       CREATE TABLE fx.spark_struct_stream_sink (
       fx_marker text,
       timestamp_ms timestamp,
       timestamp_dt date,
       primary key (fx_marker));
  */
  val namespace = "applicationlifecycle"
  val foreachTableSink = "prediction_"
}