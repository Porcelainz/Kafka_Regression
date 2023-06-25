package Experiment4
import scala.collection.mutable.ListBuffer
import com.han.ATree
import scala.util.hashing.MurmurHash3
object Experiment4_2 {
  def generateID(_expression: String): Long = {
    // val predicatesAndOperators: List[Char] = _expression.toList
    // var id: Int = 0
    
    
    // for (char <- predicatesAndOperators) {
    //   id += char.hashCode() * char.hashCode()
    // }
    val seed = 0 // Seed value for the hash function
    val components = _expression.split('^').sorted.mkString("^") // Sort components for consistent ordering
    val hash:Long = MurmurHash3.stringHash(components, seed)

    hash

    //id
  }
  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()
    val q0 = "BTC>1^SOL>3"
    val q2 = "BTC>1^SOL>3^SOL>4"
    
    val q1 = "BTC>1^SOL>3^FTT>4"
    query_set += q0
    query_set += q2
    query_set += q1 

    val tree = new ATree("Experiment_4")
    query_set.foreach(x => tree.add_query(x))
    tree.hen(generateID(q1)).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
    tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
    //println(tree.hen(generateID(q1)).childs)
    val inputValues = Array("BTC:11", "SOL:11", "FTT:12") 
    val event_happen_time = System.currentTimeMillis() 
    //inputValues.foreach( x =>  {tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble) ; Thread.sleep(5000)})
    for (i <- 0 until(inputValues.length)) {
      tree.switch_Node_Map(inputValues(i).split(":").head).receiveValueThenForward(inputValues(i).split(":").last.toDouble)
      //Thread.sleep(5000)
    }
    val trigger_time = System.currentTimeMillis()
    println(s"Latency: ${trigger_time - event_happen_time} ms")
  
  }
}