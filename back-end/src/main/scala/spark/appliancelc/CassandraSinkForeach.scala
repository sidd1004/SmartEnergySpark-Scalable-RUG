package spark.appliancelc

import org.apache.spark.sql.ForeachWriter

class CassandraSinkForeach(algorithm: String) extends ForeachWriter[org.apache.spark.sql.Row] {
  // This class implements the interface ForeachWriter, which has methods that get called
  // whenever there is a sequence of rows generated as output
  val cassandraDriver = new CassandraDriver();

  def open(partitionId: Long, version: Long): Boolean = {
    // open connection
    println(s"Open connection")
    true
  }

  def process(record: org.apache.spark.sql.Row) = {
    println(s"Process new $record")
    cassandraDriver.connector.withSessionDo(session =>
                                              session.execute(
                                                s"""
       insert into ${cassandraDriver.namespace}.${cassandraDriver.foreachTableSink + algorithm} (uniqueid, applianceid, applianceage, comments, energy, predicted_age)
       values('${record(0)}', '${record(1)}', '${record(2)}', '${record(3)}', '${record(4)}', '${record(5)}')""")
                                            )
  }

  def close(errorOrNull: Throwable): Unit = {
    // close the connection
    println(s"Close connection")
  }
}