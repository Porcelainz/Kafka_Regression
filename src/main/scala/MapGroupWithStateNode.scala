import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession,Dataset}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.streaming.{GroupState, GroupStateTimeout}
import org.apache.spark.sql.SQLContext
import scalaj.http._
class MyNode1(_name:String) extends Serializable{
  val name = _name
  var count: Int = 0
  var parent: MyNode = _
  
}
case class MyNode(_name:String, var count:Int) extends Serializable{
  val name = _name
  //var count: Int = _count
  var parent: MyNode = _
  var url: String = ""
  val limitation = 10
  def setParent(parent: MyNode): Unit = {
    this.parent = parent
  }
  def propagate(value: Int): Unit = {
    if (parent != null) {
      parent.count += value
      if (parent.count == limitation) {
        parent.sendNotification()
      }
      parent.propagate(value)
    }
  }

  def setUrl(url: String): Unit = {
    this.url = url
  }

  def sendNotification(): Unit = {
    if (url != "" ) {
      val testRequest = Http(url).asString.body
    }
  }

}


object MapGroupWithStateNode {

  val node1 = new MyNode("node1",0)
  val node2 = new MyNode("node2",0)
  val node3 = new MyNode("node3",0)
  node3.setUrl("https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  node1.setParent(node3)
  node2.setParent(node3)
  def updateNodeAcrossEvents(key: String,
                             values: Iterator[String],
                             state: GroupState[MyNode]): Array[MyNode] = {
    var currentCount = state.getOption.map(_.count).getOrElse(0)
    var currentNode = node1
    values.foreach(x => if (x.toInt > 5) {node1.count +=  + 1; node1.propagate(1); } 
                              else {node2.count +=  + 1; node2.propagate(1);currentNode = node2})                  
    val updatedNodeCount = Array(node1, node2, node3)
    updatedNodeCount.foreach(x => println(x.name + " " + x.count))
    updatedNodeCount.foreach(x => state.update(x))
    // state.update(currentNode)
    // state.update(node3)
    updatedNodeCount
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("NodeMapGroupsWithStateExample")
      .master("local[*]")
      .getOrCreate()
      spark.sparkContext.setLogLevel("ERROR")
    //val sqlContext = new SQLContext(spark.sparkContext)
     import spark.implicits._
     
     val lines: Dataset[String] = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "9999")
      .load()
      .as[String]
      
    import spark.implicits._
      val nodeCounts = lines.groupByKey(values => values)
        .mapGroupsWithState[MyNode,Array[MyNode]](GroupStateTimeout.NoTimeout())(updateNodeAcrossEvents _)

    nodeCounts.writeStream
      .outputMode("update")
      .format("console")
      .start()
      .awaitTermination()

  }










}

