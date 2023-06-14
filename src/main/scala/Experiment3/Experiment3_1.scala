package Experiment3
import scala.collection.mutable.ListBuffer
import com.han.ATree
object Experiment3_1 {
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
    query_set += "p1=11"
    query_set += "p1>6"
    query_set += "p1>3"

    val tree = new ATree("Experiment_3")
    query_set.foreach(x => tree.add_query(x))
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
    tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
    //tree.groupBySource_Map.foreach(x => println(x._2.mkString(",")))
    val inputValues = Array("p1:11")
    println("------ Input Value = 11 ------")
    inputValues.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println("p1=11's result " + tree.hen(generateID("p1=11")).state)
    println("p1>6's result " + tree.hen(generateID("p1>6")).state)
    println("p1>3's result " + tree.hen(generateID("p1>3")).state)


    val inputValues2 = Array("p1:10")
    println("------ Input Value = 10 ------")
    inputValues2.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println("p1=11's result " + tree.hen(generateID("p1=11")).state)
    println("p1>6's result " + tree.hen(generateID("p1>6")).state)
    println("p1>3's result " + tree.hen(generateID("p1>3")).state)

    val inputValues3 = Array("p1:6")
    println("------ Input Value = 6 ------")
    inputValues3.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println("p1=11's result " + tree.hen(generateID("p1=11")).state)
    println("p1>6's result " + tree.hen(generateID("p1>6")).state)
    println("p1>3's result " + tree.hen(generateID("p1>3")).state)
    
    val inputValues4 = Array("p1:3")
    println("------ Input Value = 3 ------")
    inputValues4.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println("p1=11's result " + tree.hen(generateID("p1=11")).state)
    println("p1>6's result " + tree.hen(generateID("p1>6")).state)
    println("p1>3's result " + tree.hen(generateID("p1>3")).state)
  }
}
