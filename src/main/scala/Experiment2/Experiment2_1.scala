import org.apache.spark.util.SizeEstimator
import scala.collection.mutable.ListBuffer
import com.han.ATree
object Experiment2_1 {

  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()
    val query_set2 = ListBuffer[String]()
    val baseQueries = List(
      "P1",
      "P2",
      "P3",
      "P4",
      "P5",
      "P6",
      "P7",
      "P8",
      "P9"
    )
    def generateRandomQuery(): String = {
      val numClauses = scala.util.Random
        .between(2, 6) // Choose a random number of clauses between 2 and 5
      val clauses = ListBuffer[String]()

      for (_ <- 1 to numClauses) {
        val clauseLength = scala.util.Random.between(
          1,
          4
        ) // Choose a random length for each clause between 1 and 3
        val atoms = ListBuffer[String]()

        for (_ <- 1 to clauseLength) {
          atoms += baseQueries(
            scala.util.Random.nextInt(baseQueries.length)
          ) // Choose a random atom from the baseQueries list
        }

        // if (clauseLength > 1) {
        // clauses += s"Seq(${atoms.mkString(",")})"
        // } // Create the clause
        clauses += atoms.mkString("^")
      }

      clauses.mkString(
        "^"
      ) // Combine the clauses with the conjunction symbol "^"
    }

// Generate 100 random queries and add them to query_set
    
      for (_ <- 1 to 1000) {
        val randomQuery = generateRandomQuery()
        query_set += randomQuery
      }
      println(s"1000000 means 100K Query Number  9 Predicate Number")
      //query_set.foreach(x => println(x))
      val tree = new ATree(s"Experiment_1")
      val startTime = System.currentTimeMillis()
      
      query_set.foreach(x => tree.add_query(x))
      tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
      val endTime = System.currentTimeMillis()
      println(s"Program Run Time: ${endTime - startTime} ms")
      val memorySize = SizeEstimator.estimate(tree)
      println(s"Estimated memory size: $memorySize bytes")
      println(tree.hen.size)
      query_set.clear()
      
      for (_ <- 1 to 1000) {
        val randomQuery = generateRandomQuery()
        query_set2 += randomQuery
      }
      println(s"1000000 means 100K Query Number  9 Predicate Number")
      //query_set.foreach(x => println(x))
      val tree2 = new ATree(s"Experiment_2")
      val startTime2 = System.currentTimeMillis()
      
      query_set2.foreach(x => tree2.add_query(x))
      tree2.hen.foreach(x => tree2.checkNodeChildsParent(x._2))
      val endTime2 = System.currentTimeMillis()
      println(s"Program Run Time: ${endTime2 - startTime2} ms")
      val memorySize2 = SizeEstimator.estimate(tree2)
      println(s"Estimated memory size: $memorySize2 bytes")
      println(tree2.hen.size)
      query_set2.clear()
    
    
  
  
  
  }
}
