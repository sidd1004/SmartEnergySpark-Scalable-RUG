package spark.utility

object Constants {
  val EMPTY = ""
  val ESCAPED_QUOTE = "\""
  val APPLICATION_CONF = "application.conf"
  val TOTAL_ENERGY_CONSUMPTION = "totalEnergyConsumption"
  val TOTAL_ENERGY_GENERATION = "totalEnergyGeneration"
  val TOTAL_EXCESS_ENERGY = "totalExcessEnergy"
  val CONTINUOUS_TRAINING = "continuous.training"
  val IS_CONTINUOUS_TRAINING = "continuous_training"
  val IS_STREAM_QUERY = "stream.query"
  val MAXIMUM_ITERATIONS = "maximum.itr"
  val STREAM_TESTING_DATA_HOME = "appliance.streamtest.data"
  object Kafka {
    /* KAFAK Utils Constants */
    val ACTOR_SYSTEM = "kafka"
    val KAFKA_URL = "kafka.url"
    val AUTO_OFFSET_RESET_CONFIG = "earliest"
    val KAFKA_TOPIC = "kafka.topic"
    val EMPTY_CONSUMER_MESSAGE = "Consumer Topic is current empty"
    val PROP_GROUP_ID = "group.id"
    val KAFKA_GROUP_ID = "kafka.group.id"
    val VALUE_DESERIALIZER = "value.deserializer"
    val KEY_DESERIALIZER = "key.deserializer"
    val VALUE_SERIALIZER = "value.serializer"
    val KEY_SERIALIZER = "key.serializer"
    val VALUE_SERIALIZER_CLASS = "org.apache.kafka.common.serialization.StringSerializer"
    val KEY_SERIALIZER_CLASS = "org.apache.kafka.common.serialization.StringSerializer"
    val KAFKA_VALUE_DESERIALIZER = "kafka.value.deserializer"
    val KAFKA_KEY_DESERIALIZER = "kafka.key.deserializer"
    val KAFKA_APPLICATION_ID = "kafka.application.id"
    val KAFKA_CONSUMER_POLL_TIMEOUT = "kafka.consumer.timeout"
    val KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers"
    val SUBSCRIBE_TOPIC = "subscribe"
    val BATCH_QUERY_TOPIC = "kafka.batchquery.topic"
    val STREAM_QUERY_TOPIC = "kafka.streamquery.topic"
    val BATCH_QUERY_STARTING_OFFSET = "kafka.batchquery.startingOffset"
    val STREAM_QUERY_STARTING_OFFSET = "kafka.streamquery.startingOffset"
    val BATCH_QUERY_CHECKPOINT = "kafka.batchquery.checkpointLocation"
    val STREAM_QUERY_CHECKPOINT = "kafka.streamquery.checkpointLocation"
    val KEY = "key"
    val VALUE = "value"
    val deserializeKeyToString = "CAST(key AS STRING)"
    val deserializeValueToString = "CAST(value AS STRING)"
    val STARTING_OFFSETS = "startingOffsets"
    val TOPIC = "topic"
    val CHECKPOINT_LOCATION = "checkpointLocation"
  }

  val JSON_DATA = "jsonData"
  val ALGORITHM_TYPE = "algorithm.type"
  object Cassandra {
    /* Cassandra Utils Constants */
    val CASSANDRA_HOTS = "cassandra.datagen.hosts"
    val CASSANDRA_PORT = "cassandra.datagen.port"
    val CASSANDRA_KEYSPACE = "cassandra.datagen.keyspace"
    val CASSANDRA_REPLICATION_FACTOR = "cassandra.datagen.replication"
    val CASSANDRA_RECONNECTION_DELAY = 1000
    val CASSANDRA_MAXIMUM_RETRY = 10
    val DURABLE_WRITES_FLAG = "cassandra.datagen.durable_writes"
    val CASSANDRA_TABLE_NAME = "cassandra.datagen.table_name"
    val CASSANDRA_TABLE_SCHEMA = "cassandra.datagen.table.schema"
    val CASSANDRA_KEYSPACE_CLASS_TYPE = "cassandra.datagen.keyspace.class"
    val SPARK_CASSANDRA_HOST = "spark.cassandra.connection.host"
    val SPARK_CASSANDRA_PORT = "spark.cassandra.connection.port"
  }

  val LOG_LEVEL = "loglevel"
  val SPARK_MASTER = "spark.master"
  val REFRIGERATION = "refrigeration"
  val PLUGLOADS = "plugloads"
  val EVCHARGE = "evcharge"
  val OTHERS = "others"
  val LOWER_LIMIT = "lowerLimit"
  val UPPER_LIMIT = "upperLimit"
  val DEFAULT_LOAD_LIMIT = 100
  val WARRANTY_DATE = "Warranty_date"
  val LABEL = "Label"
  val LABELS = "labels"
  val TRAINING = "training"
  val TESTING = "testing"
  val ENERGY = "energy"
  val DISTANCE = "distance"
  val LEFT_JOIN = "left"
  val ROW_NUMBER = "row_number"

  object Appliances {
    val DISH_WASHER = "Dish Washer"
    val FURNACE = "Furnace"
    val FRIDGE = "Fridge"
    val WINE_CELLAR = "Wine Cellar"
    val MICROWAVE = "Microwave"
    val GARAGE_DOOR = "Garage door"
  }

  val CAPTURE_DATE = "capture_date"
  val PURCHASE_DATE = "purchase_date"
  val APPLIANCE_AGE = "applianceage"
  val APPLIANCE_AGE_TEST = "appliance_age_test"
  val MEAN_APPLIANCE_AGE = "mean_appliance_age"
  val PREDICTED_AGE = "predicted_age"
  val COMMENTS = "comments"
  val APPLIANCE_ID = "applianceid"
  val PROTOTYPE_ID = "prototype_id"
  val APPLIANCE = "Appliance"
  val UNIQUE_ID = "uniqueid"
  val FILE_NAME = "fileName"

  val PROTOTYPE_APPLIANCE_AGE = "proptotype_appliance_age"
  val PROTOTYPE_ENERGY = "portotype_energy"
  val OLD_PROTOTYPE_APPLIANCE_AGE = "old_proptotype_appliance_age"
  val OLD_PROTOTYPE_ENERGY = "old_portotype_energy"
  val OLD_PROTOTYPE_VECTOR = "old_prototype_vector"
  val PROTOTYPE_VECTOR = "prototype_vector"
  val VECTOR = "vector"
  val VARIATION = "variation"
  val MIN_DIST = "minDist"

  val FEATURE_VECTOR_TRAINING = "vectorTR"
  val FEATURE_VECTOR_TO_PREDICT = "vectorTe"

  val TRUE = "true"

  object CSV {
    val HEADER = "header"
    val DELIMITER = "delimiter"
  }

  object DateFormats {
    val YYYY_MM_DD = "yyyy-MM-dd"
    val MMDDYYYY = "MM-dd-yyyy"
  }

  val TEST_DATA_PATH = "appliance.testing.data"

}

object Delimiters {
  val COMMA = ","
  val COLON = ":"
}
