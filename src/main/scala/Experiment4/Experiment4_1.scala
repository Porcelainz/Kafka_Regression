package Experiment4
import scala.collection.mutable.ListBuffer
import com.han.ATree
object Experiment4_1 {
  def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
    }

    id

  }
  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()
    val q1 = "Seq(q1.slope=11,q1.slope=-3,q1.slope=12)"
    query_set += q1 

    val tree = new ATree("Experiment_4")
    query_set.foreach(x => tree.add_query(x))
    tree.hen(generateID(q1)).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
    tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
    println(tree.hen(generateID(q1)).childs)
    val inputValues = Array("q1.slope:11", "q1.slope:-3", "q1.slope:12")  
    inputValues.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))

  
  }
}