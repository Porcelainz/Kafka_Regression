import scalaj.http._
import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.log4j.{Level, Logger}
import spire.std.float


class NotificationNode(_name: String, _url: String = "") extends Serializable {
  var name = _name
  var parent: NotificationNode = _ 
  var result: Boolean = false
  val url: String = _url 
  def setParent(parent: NotificationNode): Unit = {
    this.parent = parent
  }

  def changeResult(result: Boolean): Unit = {
    this.result = result
    println(name + result)
    propagateResult
    //println(name + result)
  }
  

  def checkResult: Unit = {
    if (this.result == true) {
      sendNotification()
    }

    def sendNotification():Unit = {
      if (!this.url.isEmpty()) {
        val testRequest = Http(this.url).asString.body
      }
    }
    
  }

  def propagateResult: Unit = {
    if (this.parent != null) {
      this.parent.changeResult(this.result)
      this.parent.checkResult
    }
  }



}




class StreamingNode(appName: String, topic: String) extends Serializable{

  val conf = new SparkConf()
    .setAppName(appName)
    .setMaster("local[*]")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  val kafkaServers = "localhost:9092"
  val kafkaTopic = topic

  val inputDf = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaServers)
    .option("subscribe", kafkaTopic)
    .load()

  val valueDf = inputDf.selectExpr("CAST(value AS STRING)")

  import org.apache.spark.sql.functions._
  // val dataDf = valueDf.select(split(col("value"), ",").getItem(0).cast("double").alias("input"),
  //   split(col("value"), ",").getItem(1).cast("double").alias("output"))

  val dataDf = valueDf.select(
    col("value").cast("String").alias("IoT-Value from: " + topic)
  )
  println(dataDf.schema)
  
  val child = new NotificationNode("child", "")
  val parent = new NotificationNode("parent", "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  child.setParent(parent)



  //my data format is string so if the row's value is scala_event then send a request to IFTTT
  val query = dataDf.writeStream.foreachBatch((batchDF: DataFrame, batchId: Long) =>
    if (!batchDF.isEmpty) {
      batchDF.foreach(x =>
        x(0).toString().toDouble match {
          case d:Double if (d > 5.0) =>  child.changeResult(true)
            //  val testRequest = Http(
            //   "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM"
            // ).asString.body
          case _ => println("No match")
        }
        // x(0) match {
        //   case "scala_event" =>
        //     val testRequest = Http(
        //       "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM"
        //     ).asString.body
        //   case s:String => 
        //   case _ => 
        // }
      )
    }
  ).start()
// it work ! 


  // val query = dataDf.writeStream.foreachBatch((batchDF: DataFrame, batchId: Long) =>
  //     if (!batchDF.isEmpty) {
  //       dataDf.foreach(x =>
  //         x(0) match {
  //           case "scala_event" =>
  //             val testRequest = Http(
  //               "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM"
  //             ).asString.body
  //           case _ => println("No match")
  //         }
  //       )
  //     }
  //   ).start()
  //val query = dataDf.writeStream.outputMode("append").format("console").start()
    //.trigger(Trigger.ProcessingTime("5 seconds"))
    // .start()
  def run(): Unit = {

    query.awaitTermination()
  }

}
