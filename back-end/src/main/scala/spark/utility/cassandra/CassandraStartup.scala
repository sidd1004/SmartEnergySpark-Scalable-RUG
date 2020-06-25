package spark.utility.cassandra

import java.net.InetSocketAddress
import com.datastax.driver.core.{Cluster, Session}

object CassandraStartup {

  /**
   * Initialize the Cassandra Database by using parameters
   *
   * @param port              -> Cassandra Port
   * @param keyspace          -> Cassandra Keyspace to use/create
   * @param table             -> Cassandra Table name to use/create
   * @param replicationFactor ->  Replication Factor of the Cassandra Keyspace; default = 1
   * @return -> Cassandra Cluster object
   */
  def init(uri: Seq[String], port: Int, keyspace: String, table: String, replicationFactor: Int = 1): Session = {
    val c = Cluster.builder().addContactPointsWithPorts(new InetSocketAddress(uri.head, port)).build()
    val session = c.connect()
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': '$replicationFactor'};")

    if (table == "prediction_kmeans") {
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspace.$table(uniqueid text, applianceage text, energy text, applianceid text, comments text, predicted_age text, PRIMARY KEY (uniqueid, applianceid));")
    } else if (table == "prediction_knn") {
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspace.$table(uniqueid text, applianceage text, energy text, applianceid text, comments text, predicted_age text, PRIMARY KEY (uniqueid, applianceid));")
    }
    else if (table == "prediction_kmeans") {
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspace.$table(uniqueid text, applianceid text, applianceage text, energy text, PRIMARY KEY (uniqueid));")
    }
    else if (table == "training") {
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspace.$table(uniqueid text, applianceid text, applianceage text, energy text, PRIMARY KEY (uniqueid));")

    }
    else if (table == "execution_logs") {
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspace.$table(timestamp text, stage text, exec_time int, PRIMARY KEY (timestamp));")
    }
    else {
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspace.$table(applianceid text, appliancename text, PRIMARY KEY (applianceid));")
      session.execute(s"insert into $keyspace.$table(applianceid, appliancename) values('0', 'Dish Washer')")
      session.execute(s"insert into $keyspace.$table(applianceid, appliancename) values('1', 'Furnace')")
      session.execute(s"insert into $keyspace.$table(applianceid, appliancename) values('2', 'Fridge')")
      session.execute(s"insert into $keyspace.$table(applianceid, appliancename) values('3', 'Wine')")
      session.execute(s"insert into $keyspace.$table(applianceid, appliancename) values('4', 'Microwave')")
      session.execute(s"insert into $keyspace.$table(applianceid, appliancename) values('5', 'Garage door')")
    }
    c.connect(keyspace)
  }

}