import scalaj.http._
import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.log4j.{Level, Logger}
import spire.std.float
import java.io._
import org.apache.hadoop.shaded.org.checkerframework.checker.units.qual.s


class NotificationNode(_name: String, _url: String = "") extends Serializable {
  var name = _name
  var parent: NotificationNode = _ 
  var result: Boolean = false
  val url: String = _url 
  var trueCount = readCountFromFile()
  var limitation = 10

  private def readCountFromFile(): Int = {
    val file = new File(s"$name.txt")
    if (file.exists()) {
      val source = scala.io.Source.fromFile(file)
      val count = source.mkString.toInt
      source.close()
      count
    } else {
      0
    }
  }
  private def writeNodeCountToFile(count: Int): Unit = {
    val file = new File(s"$name.txt")
    val writer = new PrintWriter(file)
    writer.write(count.toString)
    writer.close()
  }













  def setParent(parent: NotificationNode): Unit = {
    this.parent = parent
  }

  def changeResult(result: Boolean): Unit = {
    this.result = result
    trueCount = readCountFromFile() + 1
    writeNodeCountToFile(trueCount)
    println(name + ": "+ result)
    println(name + ": " + trueCount)
    propagateResult
    //println(name + result)
  }
  

  def checkResult: Unit = {
    if (this.trueCount == limitation) {
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
      this.parent.changeResult(true)
      this.parent.checkResult
    }
  }



}



class StreamingNode(appName: String, topic: String, node1: NotificationNode, node2: NotificationNode) extends Serializable{
  //var counter = 0
  // val node_1 = node1
  // val node_2 = node2


  val conf = new SparkConf()
    .setAppName(appName)
    .setMaster("local[*]")
    .set("spark.streaming.stopGracefullyOnShutdown", "true")
  val spark = SparkSession.builder().config(conf).getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")
  spark.conf.set("spark.sql.streaming.checkpointLocation", "checkpoint")
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
  
  // val child1 = new NotificationNode("child1", "")
  // val child2 = new NotificationNode("child2", "") 
  //val parent = new NotificationNode("parent", "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  // child1.setParent(parent)
  // child2.setParent(parent)

  //val childs = List(child1, child2)


  //my data format is string so if the row's value is scala_event then send a request to IFTTT
  val query = dataDf.writeStream.foreachBatch((batchDF: DataFrame, batchId: Long) =>
    if (!batchDF.isEmpty) {
      batchDF.foreach(x =>
        x(0).toString().toDouble match {
          case d:Double if (d > 5.0) =>  node1.changeResult(true)
          case d:Double if (d < 3.0) =>  node2.changeResult(true)
            //  val testRequest = Http(
            //   "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM"
            // ).asString.body
          case _ => println("Counter: ")
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


