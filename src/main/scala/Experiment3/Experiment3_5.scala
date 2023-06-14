package Experiment3
import scala.collection.mutable.ListBuffer
import com.han.ATree
object Experiment3_5 {
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
    val q1 = "p1<20"
    val q2 = "p1=13"
    val q3 = "p1>10"
    query_set += q1
    query_set += q2
    query_set += q3

    val tree = new ATree("Experiment_3")
    query_set.foreach(x => tree.add_query(x))
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
    tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
    //tree.groupBySource_Map.foreach(x => println(x._2.mkString(",")))
    val inputValues = Array("p1:13")
    println("------ Input Value = 13 ------")
    inputValues.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println(q1 + "'s result " + tree.hen(generateID(q1)).state)
    println(q2 + "'s result " + tree.hen(generateID(q2)).state)
    println(q3 + "'s result " + tree.hen(generateID(q3)).state)


    val inputValues2 = Array("p1:15")
    println("------ Input Value = 15 ------")
    inputValues2.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println(q1 + "'s result " + tree.hen(generateID(q1)).state)
    println(q2 + "'s result " + tree.hen(generateID(q2)).state)
    println(q3 + "'s result " + tree.hen(generateID(q3)).state)

    val inputValues3 = Array("p1:20")
    println("------ Input Value = 20 ------")
    inputValues3.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println(q1 + "'s result " + tree.hen(generateID(q1)).state)
    println(q2 + "'s result " + tree.hen(generateID(q2)).state)
    println(q3 + "'s result " + tree.hen(generateID(q3)).state)
    
    val inputValues4 = Array("p1:7")
    println("------ Input Value = 7 ------")
    inputValues4.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
    println(q1 + "'s result " + tree.hen(generateID(q1)).state)
    println(q2 + "'s result " + tree.hen(generateID(q2)).state)
    println(q3 + "'s result " + tree.hen(generateID(q3)).state)
  }
}
