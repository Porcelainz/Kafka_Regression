import org.apache.hadoop.shaded.org.checkerframework.checker.units.qual.s
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.GroupState
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.streaming._
import scalaj.http._
import spire.std.float

import java.io._

class NotificationNode(_name: String, _url: String = "") extends Serializable {
  
  var name = _name
  var parent: NotificationNode = _ 
  var result: Boolean = false
  val url: String = _url 
  var trueCount = 0
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
    trueCount = trueCount + 1
    
    println(name + ": "+ result)
    println(name + ": " + trueCount)
    propagateResult
    println(name + ": " + result)
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







class StreamingNode(appName: String, topic: String) extends Serializable{
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

  // val inputDf = spark.readStream
  //   .format("kafka")
  //   .option("kafka.bootstrap.servers", kafkaServers)
  //   .option("subscribe", kafkaTopic)
  //   .load()

  // val valueDf = inputDf.selectExpr("CAST(value AS STRING)")

  import org.apache.spark.sql.functions._
  // val dataDf = valueDf.select(split(col("value"), ",").getItem(0).cast("double").alias("input"),
  //   split(col("value"), ",").getItem(1).cast("double").alias("output"))

  // val dataDf = valueDf.select(
  //   col("value").cast("String").alias("IoT-Value from: " + topic)
  // )
  //println(dataDf.schema)
  
  val child1 = new NotificationNode("child1", "")
  val child2 = new NotificationNode("child2", "") 
  val parent = new NotificationNode("parent", "https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  child1.setParent(parent)
  child2.setParent(parent)
  
  import spark.implicits._
  def updateStateWithEvent(key: String, events: Iterator[Double], state: GroupState[NotificationNode]): NotificationNode = {
  val currentNode = if (state.exists) state.get else new NotificationNode(key, "")
  var result = currentNode.result
  events.foreach(event => {
    if (event > 5) {
      child1.changeResult(true)
      result = true
    } else if (event < 3) {
      child2.changeResult(true)
      result = true
    }
  })
  state.update(currentNode)
  currentNode
}
 val events = spark.readStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "localhost:9092")
  .option("subscribe", topic)
  .load()
  .selectExpr("CAST(value AS STRING)")
  .as[String]
  .map(_.toDouble) // convert the string to Int

  
  
  import org.apache.spark.sql.streaming.GroupState
  import spark.implicits._
  import org.apache.spark.sql.Encoders
  implicit val notificationNodeEncoder = Encoders.kryo[NotificationNode]
  val statefulQuery = events
  .groupByKey(_.toString)
  .mapGroupsWithState[NotificationNode, NotificationNode](GroupStateTimeout.ProcessingTimeTimeout())(updateStateWithEvent _)
  .writeStream
  .trigger(Trigger.ProcessingTime("10 seconds"))
  .outputMode("update")
  .format("console")
  .start()

  



 
  def run(): Unit = {

    statefulQuery.awaitTermination()
  }

}


