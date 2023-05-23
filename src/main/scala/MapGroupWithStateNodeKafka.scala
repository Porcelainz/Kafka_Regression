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






object MapGroupWithStateNodeKafka {


    def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
    }

    id
  }

  val tree = new ATree("you ATree")
  tree.insert("BTC>3^ETH>9^DOGE>10")
  tree.insert("BTC>3^ETH>9^SOL>20")
  tree.insert("BTC.Slope>3^ETH>9^SOL>20")
  tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
  tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
  println("-----------------------")
  tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  tree.hen(generateID("BTC>3^ETH>9^SOL>20")).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  tree.hen(generateID("BTC.Slope>3^ETH>9^SOL>20")).setUrl("https://maker.ifttt.com/trigger/Slope_Event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).url)
  tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<'))
  tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
  println(tree.groupBySource_Map)
  //val groupMap2 = groupMap.map(x => (x._1, x._2.map(_.expression)))
  //println(groupMap2)
  println("-----------1111111111111111111------------")
  
  def updateNodeAcrossEvents(key: String,
                             values: Iterator[String],
                             state: GroupState[ATree]): ATree = {
    
    var currentTree = state.getOption.getOrElse(new ATree("1"))
    
                              
   
    values.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    val updatedTree = tree
    state.update(tree)
    tree
  }

  
  def main(args: Array[String]): Unit = {
    println("30000000")
    val spark = SparkSession.builder()
      .appName("NodeMapGroupsWithStateTreeExample")
      .master("local[*]")
      .getOrCreate()
      spark.sparkContext.setLogLevel("ERROR")
    
    //import spark.implicits._
    val kafkaParams = Map[String, String](
      "kafka.bootstrap.servers" -> "localhost:9092",
      "subscribePattern" -> "^MyTest-.+" // Pattern to match topics with names starting with "topic-" followed by a digit
      )

    import spark.implicits._
     val lines = spark.readStream
      .format("kafka")
      .options(kafkaParams)
      .load()
      .selectExpr("CAST(value AS STRING) AS value")
      .as[String]
    
    //import spark.implicits._
   
    val atree = lines
        .groupByKey(values => values)
        .mapGroupsWithState[ATree,ATree](GroupStateTimeout.NoTimeout())(updateNodeAcrossEvents _)  


   
    atree.writeStream
      .outputMode("update")
      .format("console")
      .start()
      .awaitTermination()
  

  }
}

