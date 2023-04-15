import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession,Row}
import org.apache.spark.sql.functions.{from_json, lit, struct, to_json}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.SparkConf

object SparkRegression extends App {

val bootstrapServers = "localhost:9092"
val inputTopic = "temp-topic"
val outputTopic = "slope-topic"
val windowDuration = "5 seconds"
val slideDuration = "5 seconds"



val conf = new SparkConf()
    .setAppName("slope")
    .setMaster("local[*]")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

import spark.implicits._

// Read streaming data from Kafka
val inputDF = spark.readStream
  .format("kafka")
  .option("kafka.bootstrap.servers", bootstrapServers)
  .option("subscribe", inputTopic)
  .load()
  .selectExpr("CAST(value AS STRING) as temp")

// Convert the input data to double and add a timestamp column
val doubleDF = inputDF
  .select($"temp".cast("double").alias("temp"), current_timestamp().alias("timestamp"))

// Define a UDF to calculate the slope using linear regression
val slopeUDF = udf { values: Seq[Row] =>
  val lr = new LinearRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)

  val data = values.map { r =>
    (r.getDouble(0), r.getLong(1).toDouble)
  }

  val dataDF = spark.createDataFrame(data).toDF("temp", "timestamp")

  val model = lr.fit(dataDF)

  model.coefficients(0)
}

// Compute the slope for each window and output to Kafka
val slopeDF = doubleDF
  .withColumn("index", monotonically_increasing_id())
  .groupBy(window($"timestamp", windowDuration, slideDuration))
  .agg(slopeUDF(collect_list(struct("temp", "index"))).alias("slope"))
  .select($"window.start".alias("start"), $"window.end".alias("end"), $"slope")
  .selectExpr("CAST(start AS STRING)", "CAST(end AS STRING)", "CAST(slope AS STRING)")
  .writeStream
  .outputMode("update")
  .format("kafka")
  .option("kafka.bootstrap.servers", bootstrapServers)
  .option("topic", outputTopic)
  .option("checkpointLocation", "/tmp/checkpoint")
  .trigger(Trigger.ProcessingTime(slideDuration))
  .start()

  slopeDF.awaitTermination()
}
