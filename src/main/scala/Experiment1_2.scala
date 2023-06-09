import scala.util.matching.Regex

object Experiment1_2 {
  def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
    }

    id

  }
  def main(args: Array[String]): Unit = {
    val tree = new ATree("Experiment_1")
    val query1 = "P1^P2^P3"
    val query2 = "P4^P1^P2"
    val query3 = "P1^P2^P3^P4"
    tree.add_query(query1)
    tree.add_query(query2)
    tree.add_query(query3)


    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    println("------------------Start----------------------------")
    tree.leafNodeArrayBuffer.foreach(x => println(x.expression))
    val regex = new Regex("([A-Za-z]+)[<>]\\d+")
    val groupMap = tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<'))
    val groupMap2 = groupMap.map(x => (x._1, x._2.map(_.expression)))
    println(groupMap2)
    println("-----------------------")
    tree.hen.foreach(x => println(x._2.expression))
    println("-----------------------")
    
    println(query1 + "'s childs: "  +tree.hen(generateID(query1)).childs)
    println(query2 + "'s childs: "  +tree.hen(generateID(query2)).childs)
    println(query1 + "'s parents: "  +tree.hen(generateID(query1)).parents)
    println(query3 + "'s childs: "  +tree.hen(generateID(query3)).childs)
    println("P1^P2's childs: "  +tree.hen(generateID("P1^P2")).childs)


  }
}
