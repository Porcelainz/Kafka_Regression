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
  @transient
  var limitation = name.replace("^","").replace("∨","").length
  var result: Boolean = _
  def setParent(parent: MyNode): Unit = {
    this.parent = parent
  }
  def propagate(value: Boolean, comes_from: String): Unit = {
    
    if (parent != null) {
      if (value) {
        parent.count += 1
      } else {

        if (parent.count != 0) {parent.count -= 1}
      }
      if (parent.count == parent.limitation) {
        println(parent.limitation + " <- this is limitation" + "FROM: " + parent.name)
        parent.sendNotification()
      } 
      parent.propagate(value,this.name)
    }
  }

  def setUrl(url: String): Unit = {
    this.url = url
  }

  def sendNotification(): Unit = {
    if (url != "" ) {
      val testRequest = Http(url).asString.body
      println("Notification sent <- from" + this.name)
      println(count)
      count = 0
    }
  }

  def changeResult(result: Boolean): Unit = {
    this.result = result
    
    propagate(result, this.name)
  }


}



object MapGroupWithStateNode_test {

  val node1 = new MyNode("A^B^C",0) //BTC>5 ^ BTC >9 ∨ ETH< 3
  val node2 = new MyNode("A^B",0) //BTC>5 ^ BTC >9
  val node3 = new MyNode("A",0)//BTC >5
  val node4 = new MyNode("B",0)// BTC> 9
  val node5 = new MyNode("C",0)  //ETH< 3
  node1.setUrl("https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  node3.setParent(node2)
  node4.setParent(node2)
  node2.setParent(node1)
  node5.setParent(node1)
  def updateNodeAcrossEvents(key: String,
                             values: Iterator[String],
                             state: GroupState[MyNode]): Array[MyNode] = {
    var currentCount = state.getOption.map(_.count).getOrElse(0)
    var currentNode = node1
    
    
    values.foreach(x => if(x.split(":").head == "BTC") {
      x.split(":").last.toInt match {
        case i if (i > 5 && i <= 9) => node3.changeResult(true);node4.changeResult(false);
        case i if i > 9 => node3.changeResult(true); node4.changeResult(true); 
        case _ => node3.changeResult(false); node4.changeResult(false);
      }
    } else if (x.split(":").head == "ETH") {
      if (x.split(":").last.toInt < 3) {
        node5.changeResult(true)
      } else {
        node5.changeResult(false)
      }
    }      )          

    val updatedNodeCount = Array(node1, node2, node3, node4, node5)
    // updatedNodeCount.foreach(x => println(x.name + " " + x.count))
    // updatedNodeCount.foreach(x => state.update(x))
    // state.update(currentNode)
    // state.update(node3)
    //but it seems if i did not update the state, the state will  be updated too (?       
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
        .mapGroupsWithState[MyNode,Array[MyNode]](GroupStateTimeout.NoTimeout())(updateNodeAcrossEvents _).flatMap(x => x)

    nodeCounts.writeStream
      .outputMode("update")
      .format("console")
      .start()
      .awaitTermination()

  }










}