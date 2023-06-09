
import scala.collection.mutable.ListBuffer

object Experiment2_1 {
  
  def main(args: Array[String]): Unit = {
  val query_set = ListBuffer[String]()
  val baseQueries = List(
  "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9"
)
  def generateRandomQuery(): String = {
  val numClauses = scala.util.Random.between(2, 6) // Choose a random number of clauses between 2 and 5
  val clauses = ListBuffer[String]()

  for (_ <- 1 to numClauses) {
    val clauseLength = scala.util.Random.between(1, 4) // Choose a random length for each clause between 1 and 3
    val atoms = ListBuffer[String]()

    for (_ <- 1 to clauseLength) {
      atoms += baseQueries(scala.util.Random.nextInt(baseQueries.length)) // Choose a random atom from the baseQueries list
    }

    // if (clauseLength > 1) {
    // clauses += s"Seq(${atoms.mkString(",")})"
    // } // Create the clause
    clauses += atoms.mkString("^")
  }

  clauses.mkString("^") // Combine the clauses with the conjunction symbol "^"
}

// Generate 100 random queries and add them to query_set
for (_ <- 1 to 1000000) {
  val randomQuery = generateRandomQuery()
  query_set += randomQuery
}
  println("1000000 100w" )
  //query_set.foreach(x => println(x))
  val startTime = System.currentTimeMillis()
  val tree = new ATree("Experiment_1")
  query_set.foreach(x => tree.add_query(x))
  tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
  val endTime = System.currentTimeMillis()
  println(s"Program Run Time: ${endTime - startTime} ms")
  }
}
