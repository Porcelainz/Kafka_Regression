import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession,Dataset}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.streaming.{GroupState, GroupStateTimeout}
import org.apache.spark.sql.SQLContext
import scalaj.http._
import org.apache.spark.sql.{Dataset, Encoder, Encoders}
import scala.reflect.runtime.universe.TypeTag
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.catalyst.expressions.Encode
import scala.collection.mutable.HashMap






object MapGroupWithStateNode {


    def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
    }

    id
  }

  val tree = new ATree("My ATree")
  tree.insert("BTC>3^ETH>9^DOGE=10")
  tree.insert("BTC>3^ETH>9^DOGE>8")
  tree.insert("BTC.Slope>3^ETH>9^SOL>20")
  tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
  tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
  println("-----------------------")
  tree.hen(generateID("BTC>3^ETH>9^DOGE=10")).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  tree.hen(generateID("BTC>3^ETH>9^DOGE>8")).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  tree.hen(generateID("BTC.Slope>3^ETH>9^SOL>20")).setUrl("https://maker.ifttt.com/trigger/Slope_Event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  println(tree.hen(generateID("BTC>3^ETH>9^DOGE>8")).url)
  tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
  tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
  tree.groupBySource_Map.foreach(x => println(x._1))
  //val groupMap2 = groupMap.map(x => (x._1, x._2.map(_.expression)))
  //println(groupMap2)
  println("-----------------------")
  
  def updateNodeAcrossEvents(key: String,
                             values: Iterator[String],
                             state: GroupState[ATree]): ATree = {
    
    var currentTree = state.getOption.getOrElse(new ATree("1"))
    
                              
    // values.foreach(x => if (x.split(":").head == "BTC") {
    //   tree.groupBySource_Map("BTC").foreach(x => x.receiveResult(true))
    // } else if (x.split(":").head == "ETH") {
    //   tree.groupBySource_Map("ETH").foreach(x => x.receiveResult(true))
    // } else if (x.split(":").head == "DOGE") {
    //   tree.groupBySource_Map("DOGE").foreach(x => x.receiveResult(true))
    // } else if (x.split(":").head == "SOL") {
    //   tree.groupBySource_Map("SOL").foreach(x => x.receiveResult(true))
    // })
    values.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    val updatedTree = tree
    state.update(tree)
    tree
  }



  // def updateNodeAcrossEvents_Node(key: String,
  //                            values: Iterator[String],
  //                            state: GroupState[Node]): Array[Node] = {
    
  //   var currentCount = state.getOption.map(_.trueCounter).getOrElse(0)
   
                              
  //   values.foreach(x => if (x.split(":").head == "BTC") {
  //     tree.groupMap("BTC").foreach(x => x.receiveResult(true))
  //   } else if (x.split(":").head == "ETH") {
  //     tree.groupMap("ETH").foreach(x => x.receiveResult(true))
  //   } else if (x.split(":").head == "DOGE") {
  //     tree.groupMap("DOGE").foreach(x => x.receiveResult(true))
  //   } else if (x.split(":").head == "SOL") {
  //     tree.groupMap("SOL").foreach(x => x.receiveResult(true))
  //   })
  //   //val updatedTree = tree
  //   //state.update(tree)
  //   val updateNode = tree.hen.values.toArray
  //   updateNode
  // }






  
  def main(args: Array[String]): Unit = {
  
    val spark = SparkSession.builder()
      .appName("NodeMapGroupsWithStateTreeExample")
      .master("local[*]")
      .getOrCreate()
      spark.sparkContext.setLogLevel("ERROR")
    //val sqlContext = new SQLContext(spark.sparkContext)
     import spark.implicits._
    

     val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "9998")
      .load()
      .as[String]
    //implicit val nodeEncoder: Encoder[Node] = Encoders.product[Node]
    import spark.implicits._
    // val word = lines.select(split('value, " ").as("word"))
    val atree = lines.groupByKey(values => values)
        .mapGroupsWithState[ATree,ATree](GroupStateTimeout.NoTimeout())(updateNodeAcrossEvents _)  


   
    atree.writeStream
      .outputMode("update")
      .format("console")
      .start()
      .awaitTermination()
  

  }
}

