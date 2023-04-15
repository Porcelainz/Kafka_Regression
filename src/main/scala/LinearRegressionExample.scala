import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object LinearRegressionExample {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("LinearRegressionExample").setMaster("local[*]")
    val spark = SparkSession.builder().config(conf).getOrCreate()

    // Kafka consumer properties
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "group.id" -> "linear-regression",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("iot-data")

    // Create Kafka stream
    val kafkaStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaParams("bootstrap.servers").toString)
      .option("subscribe", topics.mkString(","))
      .option("startingOffsets", "earliest")
      .load()

    // Parse data from Kafka stream
    val dataStream = kafkaStream.selectExpr("CAST(value AS STRING)")
      .select(split(col("value"), ",").getItem(0).cast("double").alias("input"),
              split(col("value"), ",").getItem(1).cast("double").alias("output"))

    // Train linear regression model
    val lr = new LinearRegression()
      .setSolver("l-bfgs")
      .setMaxIter(10)
      .setRegParam(0.0)
      .setElasticNetParam(0.0)
      .setLabelCol("output")
      .setFeaturesCol("input")

    val model = lr.fit(dataStream)

    // Print model weights (slope)
    println(s"Slope: ${model.coefficients(0)}")

    spark.streams.awaitAnyTermination()
  }
}