object Experiment2_11 extends  App{
  var seq_index = 0
  def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    if (_expression.contains("Seq")) {
      for (char <- predicatesAndOperators) {
      id += char.hashCode() * (char.hashCode() + seq_index)
      
      } 
      seq_index += 1
    } else {
      for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
      }
    }

    id
  }
  val query1 = "Seq(P2,P5)"
  val query2 = "Seq(P5,P2)"

  val tree = new ATree("Experiment_211")
  tree.add_query(query1)
  tree.add_query(query2)
  tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
  println("-----------")
  tree.hen.foreach(x => println("ID: " +x._1 + ": " +x._2.expression))


  // println("id : " +  generateID(query1))
  // println("id : " +  generateID(query2))
  // println(seq_index)
}
