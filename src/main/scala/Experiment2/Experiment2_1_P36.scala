import org.apache.spark.util.SizeEstimator
import scala.collection.mutable.ListBuffer
import com.han.ATree
import java.io.FileWriter
import scala.util.Random
object Experiment2_1_P36 {

  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()
    
    def generateRandomQuery(): String = {
      val baseQueries = List("AAPL", "MSFT" , "AMZN", "GOOGLE", "FB","INTC", "CSCO", "NFLX", "ADBE", "TSLA", "NVDA", "PYPL", "CMCSA",
    "SBUX", "HD", "QCOM", //"ZM", "INTC", "AMD", "PEP", "MRNA", "AMAT", "CHTR", "EA", "GNC", "COST", "ADBE", "MDLZ", "JD", "EBAY", "TOYOTA", "MAR", "ATVI",
    // "REGN", "ILMN", "NISSAN"
    )
      val operators = Array("<", ">", "=")
      val numClauses = scala.util.Random
        .between(2, 3) // Choose a random number of clauses between 2 and 5
      val clauses = ListBuffer[String]()
      //2 ~15
      for (_ <- 1 to numClauses) {
        val clauseLength = scala.util.Random.between(
          1,
          4
        ) // Choose a random length for each clause between 1 and 3
        val atoms = ListBuffer[String]()

        for (_ <- 1 to clauseLength) {
          val predicate = baseQueries(
            scala.util.Random.nextInt(baseQueries.length)
          )
          atoms += (predicate + operators(Random.nextInt(operators.length)) + Random.between(1, 10)) //predicate with value
          // atoms += predicate //predicate without value 
        }
        

        // if (clauseLength > 1) {
        // clauses += s"Seq(${atoms.mkString(",")})"
        // } // Create the clause
        atoms.distinct.map ( x => clauses += x)
      }

      clauses.distinct.mkString(
        "^"
      ) // Combine the clauses with the conjunction symbol "^"
    }

    


    for( i <- 0 until 1) {
      for (_ <- 1 to 10000) {
        val randomQuery = generateRandomQuery()
        query_set += randomQuery
      }
      println(s"10000 means 10000  Query Number 36 Predicate Number")
      //query_set.foreach(x => println(x))
      val tree = new ATree(s"Experiment_$i")
      val startTime = System.currentTimeMillis()
      query_set.foreach(x => tree.add_query(x))
      tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
      val endTime = System.currentTimeMillis()
      query_set.clear()
      val switch_startTime = System.currentTimeMillis()
      tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
      tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
      tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
      val switch_endTime = System.currentTimeMillis()
      
      val fw = new FileWriter("Predicate with value n10000", true) ; 
      println(s"Program Run Time: ${endTime - startTime} ms")
      val memorySize = SizeEstimator.estimate(tree)
      println(s"Estimated memory size: $memorySize bytes")
      println(tree.hen.size)
      println(tree.for_find_max_intersect_List.size)
      //tree.for_find_max_intersect_List.foreach(x => println(x._2.expression))
      fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n" + s"Estimated memory size: $memorySize bytes" + s"\n Node number: ${tree.hen.size}" +"\n\n")
      fw.write(s"Switch Node Creation Time: ${switch_endTime - switch_startTime} ms" + "\n" + s"Switch Number: ${tree.switch_Node_Map.size}" + "\n\n" )
      fw.close()
    
    } 

    // for( i <- 0 until 5) {
    //   for (_ <- 1 to 100000) {
    //     val randomQuery = generateRandomQuery()
    //     query_set += randomQuery
    //   }
    //   println(s"100000 means 100000 Query Number  36 Predicate Number")
    //   //query_set.foreach(x => println(x))
    //   val tree = new ATree(s"Experiment_$i")
    //   val startTime = System.currentTimeMillis()
      
    //   query_set.foreach(x => tree.add_query(x))
    //   tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    //   val endTime = System.currentTimeMillis()
    //   println(s"Program Run Time: ${endTime - startTime} ms")
    //   val memorySize = SizeEstimator.estimate(tree)
    //   println(s"Estimated memory size: $memorySize bytes")
    //   println(tree.hen.size)
    //   query_set.clear()
      
    //   val fw = new FileWriter("P36 n100000", true) ; 
    //   fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n" + s"Estimated memory size: $memorySize bytes" + s"\n Node number ${tree.hen.size}" +"\n\n") ; 
    //   fw.close()
    
    // } 

    

    // for( i <- 0 until 1) {
    //   for (_ <- 1 to 200000) {
    //     val randomQuery = generateRandomQuery()
    //     query_set += randomQuery
    //   }
    //   println(s"200000 means 200000 Query Number 36 Predicate Number")
    //   //query_set.foreach(x => println(x))
    //   val tree = new ATree(s"Experiment_$i")
    //   val startTime = System.currentTimeMillis()
      
    //   query_set.foreach(x => tree.add_query(x))
    //   tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    //   val endTime = System.currentTimeMillis()
    //   println(s"Program Run Time: ${endTime - startTime} ms")
    //   val memorySize = SizeEstimator.estimate(tree)
    //   println(s"Estimated memory size: $memorySize bytes")
    //   println(tree.hen.size)
    //   query_set.clear()
      
    //   val fw = new FileWriter("P36 n200000", true) ; 
    //   fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n" + s"Estimated memory size: $memorySize bytes" + s"\n Node number ${tree.hen.size}" +"\n\n") ; 
    //   fw.close()
    
    // } 

    // for( i <- 0 until 1) {
    //   for (_ <- 1 to 500000) {
    //     val randomQuery = generateRandomQuery()
    //     query_set += randomQuery
    //   }
    //   println(s"500000 means 500000 Query Number 36 Predicate Number")
    //   //query_set.foreach(x => println(x))
    //   val tree = new ATree(s"Experiment_$i")
    //   val startTime = System.currentTimeMillis()
      
    //   query_set.foreach(x => tree.add_query(x))
    //   tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    //   val endTime = System.currentTimeMillis()
    //   println(s"Program Run Time: ${endTime - startTime} ms")
    //   val memorySize = SizeEstimator.estimate(tree)
    //   println(s"Estimated memory size: $memorySize bytes")
    //   println(tree.hen.size)
    //   query_set.clear()
      
    //   val fw = new FileWriter("P36 n500000", true) ; 
    //   fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n" + s"Estimated memory size: $memorySize bytes" + s"\n Node number ${tree.hen.size}" +"\n\n") ; 
    //   fw.close()
    
    // } 
    // for( i <- 0 until 1) {
    //   for (_ <- 1 to 1000000) {
    //     val randomQuery = generateRandomQuery()
    //     query_set += randomQuery
    //   }
    //   println(s"1000000 means 1000000 Query Number  36 Predicate Number")
    //   //query_set.foreach(x => println(x))
    //   val tree = new ATree(s"Experiment_$i")
    //   val startTime = System.currentTimeMillis()
      
    //   query_set.foreach(x => tree.add_query(x))
    //   tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    //   val endTime = System.currentTimeMillis()
    //   println(s"Program Run Time: ${endTime - startTime} ms")
    //   val memorySize = SizeEstimator.estimate(tree)
    //   println(s"Estimated memory size: $memorySize bytes")
    //   println(tree.hen.size)
    //   query_set.clear()
      
    //   val fw = new FileWriter("P36 n1000000", true) ; 
    //   fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n" + s"Estimated memory size: $memorySize bytes" + s"\n Node number ${tree.hen.size}" +"\n\n") ; 
    //   fw.close()
    
    // } 

    
    // for( i <- 0 until 1) {
    //   for (_ <- 1 to 3000000) {
    //     val randomQuery = generateRandomQuery()
    //     query_set += randomQuery
    //   }
    //   println(s"3000000 means 3000000 Query Number  36 Predicate Number")
    //   //query_set.foreach(x => println(x))
    //   val tree = new ATree(s"Experiment_$i")
    //   val startTime = System.currentTimeMillis()
      
    //   query_set.foreach(x => tree.add_query(x))
    //   tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    //   val endTime = System.currentTimeMillis()
    //   println(s"Program Run Time: ${endTime - startTime} ms")
    //   val memorySize = SizeEstimator.estimate(tree)
    //   println(s"Estimated memory size: $memorySize bytes")
    //   println(tree.hen.size)
    //   query_set.clear()
      
    //   val fw = new FileWriter("P36 n3000000", true) ; 
    //   fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n" + s"Estimated memory size: $memorySize bytes" + s"\n Node number ${tree.hen.size}" +"\n\n") ; 
    //   fw.close()
    
    //} 
  }
}
