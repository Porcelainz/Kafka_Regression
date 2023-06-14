import org.apache.spark.util.SizeEstimator
import scala.collection.mutable.ListBuffer
import com.han.ATree

import java.io.FileWriter
object Experiment2_1_P18 {

  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()
    val baseQueries = List("P1","P2", "P3","P4", "P5",  "P6", "P7","P8", "P9",  "P10", "P11", "P12", "P13","P14", "P15","P16","P17","P18")
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
    

    
   

      for( i <- 0 until 10) {
      for (_ <- 1 to 10) {
        val randomQuery = generateRandomQuery()
        query_set += randomQuery
      }
      println(s"3000000 means 3000000 Query Number  18 Predicate Number")
      query_set.foreach(x => println(x))
      val tree = new ATree(s"Experiment_$i")
      val startTime = System.currentTimeMillis()
      
      query_set.foreach(x => tree.add_query(x))
      tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
      val endTime = System.currentTimeMillis()
      println(s"Program Run Time: ${endTime - startTime} ms")
      val memorySize = SizeEstimator.estimate(tree)
      println(s"Estimated memory size: $memorySize bytes")
      println(tree.hen.size)
      query_set.clear()
      
      val fw = new FileWriter("P18 n3000000", true) ; 
      fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n" + s"Estimated memory size: $memorySize bytes" + s"\n Node number ${tree.hen.size}" +"\n\n") ; 
      fw.close()
    
    } 

    
    

    
  
  
  
  }
}
