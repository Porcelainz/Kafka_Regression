import scalaj.http._
import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext._
import spire.std.float
import java.io._

object UpdateStateByKeyTest extends App {

  //val testAccumulator = sc.longAccumulator("testAccumulator")


  val conf = new SparkConf()
    .setAppName("UpdateStateByKeyTest")
    .setMaster("local[*]")
    .set("spark.streaming.stopGracefullyOnShutdown", "true")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")
  spark.conf.set("spark.sql.streaming.checkpointLocation", "checkpoint")
  val kafkaServers = "localhost:9092"
  val kafkaTopic = "Test"

  val testAccumulator = spark.sparkContext.longAccumulator("testAccumulator") //already registered
  //spark.sparkContext.register(testAccumulator, "testAccumulator")


  def UpdateStateFunc(newValues: Seq[Long], runningCount: Option[Long]): Option[Long] = {
  val count = newValues.sum + runningCount.getOrElse(0L)
  Some(count)
  }






  val inputDf = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaServers)
    .option("subscribe", kafkaTopic)
    .load()

  val valueDf = inputDf.selectExpr("CAST(value AS STRING)")

  import org.apache.spark.sql.functions._

  val dataDf = valueDf.select(
    col("value").cast("String").alias("IoT-Value from: " + "Test")
  )
  println(dataDf.schema)

  // val query = dataDf.writeStream.foreachBatch((batchDF: DataFrame, batchId: Long) =>
  //   if (!batchDF.isEmpty) {
  //     batchDF.foreach(x =>
  //       x(0).toString().toDouble match {
          
  //           //  val testRequest = Http(
  //           //   "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM"
  //           // ).asString.body
  //         case _ => println("Counter: ")
  //       }
  //       // x(0) match {
  //       //   case "scala_event" =>
  //       //     val testRequest = Http(
  //       //       "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM"
  //       //     ).asString.body
  //       //   case s:String => 
  //       //   case _ => 
  //       // }
  //     )
  //   }
  // ).start()

  val query = dataDf.writeStream.foreachBatch((batchDF: DataFrame, batchId: Long) =>
    if (!batchDF.isEmpty) {
      batchDF.foreach(x =>
        x(0).toString().toDouble match {
          case x: Double if (x > 5.0) => testAccumulator.add(1); println("Accumulator: " + testAccumulator.value)
          case x: Double if (x < 3.0) => testAccumulator.add(2); println("Accumulator: " + testAccumulator.value) 
          case _ => 
        }
      )
    }
  ).start()
  // import spark.implicits._
  // val query = dataDf.groupBy(col("key"))
  //   .agg(sum(col("value")).as("batchValue"))
    

  query.awaitTermination()





}
