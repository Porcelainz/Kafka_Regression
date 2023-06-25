import org.apache.spark.util.SizeEstimator
import scala.collection.mutable.ListBuffer
import com.han.ATree
import com.han.Switch_Node
import com.han.Leaf_Node
import com.han.Inner_Node
import java.io.FileWriter
import scala.util.Random
import java.io.PrintWriter
import scala.util.hashing.MurmurHash3
import scala.io.StdIn.readLine
object Experiment2_1_P36 {
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

  def recordErrorToFile(error: Throwable, filePath: String): Unit = {
  val writer = new PrintWriter(new FileWriter(filePath, true))
  try {
    writer.println("An error occurred:")
    writer.println(error.getMessage)
    error.printStackTrace(writer)
    writer.println("--------------------------------------")
  } finally {
    writer.close()
  }
}

  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()
    
    def generateRandomQuery(): String = {
      val baseQueries = List("AAPL", "MSFT" , "AMZN", "GOOGLE", "FB","INTC", "CSCO", "NFLX", "ADBE", "TSLA", "NVDA", "PYPL", "CMCSA",
    "SBUX", "HD", "QCOM", "ZM", "INTC", "AMD", "PEP", "MRNA", "AMAT", "CHTR", "EA", "GNC", "COST", "ADBE", "MDLZ", "JD", "EBAY", "TOYOTA", "MAR", "ATVI",
     "REGN", "ILMN", "NISSAN", "DONG", "SOL"
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
            scala.util.Random.between(0,baseQueries.length)
          )
          atoms += (predicate + operators(Random.nextInt(operators.length)) + Random.between(1, 30)) //predicate with value
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

    
    val query_num = 3000000
    try {
  // Code that may throw an exception

    for( i <- 0 until 1) {
      for (_ <- 1 to query_num) {
        val randomQuery = generateRandomQuery()
        query_set += randomQuery
      }
      println(s"$query_num means $query_num Query Number 36  Source with 1~30 value")
      //query_set.foreach(x => println(x))
      val tree = new ATree(s"Experiment_$i")
      val startTime = System.currentTimeMillis()
      query_set.foreach(x => tree.add_query(x))
      tree.add_query("AAPL>13^GOOGLE>20^FB<15")
      tree.hen(generateID("AAPL>13^GOOGLE>20^FB<15")).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
      //tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
      val endTime = System.currentTimeMillis()
      query_set.clear()
      val switch_startTime = System.currentTimeMillis()
      tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
      tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
      tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
      val switch_endTime = System.currentTimeMillis()
      
      val fw = new FileWriter(s"Predicate with value n$query_num", true) ; 
      
      
      
      //tree.for_find_max_intersect_List.foreach(x => println(x._2.expression))
      fw.write(s"Program Run Time: ${endTime - startTime} ms" + "\n")
      fw.write(s"Whole tree Node number: ${tree.hen.size}" +"\n" + s"non leaf node number: ${tree.non_leaf_node.size}" + "\n" + s"leaf_node number: ${tree.leafNodeArrayBuffer.size}" + "\n")
      fw.write(s"shared node number: ${tree.non_leaf_node.size - query_num} \n")
      fw.write(s"Switch Node Creation Time: ${switch_endTime - switch_startTime} ms" + "\n" + s"Switch Number: ${tree.switch_Node_Map.size}" + "\n\n" )

      //
      
      
      
      //val fw2 = new FileWriter(s"Predicate with value n$query_num", true) ; 
      //val memorySize = SizeEstimator.estimate(tree)
      val runtime = Runtime.getRuntime
      val memorySize = runtime.totalMemory - runtime.freeMemory
      fw.write(s"Estimated memory size: $memorySize bytes \n")
      fw.write("---------------------------------------------------------\n")
      println(s"Program Run Time: ${endTime - startTime} ms")
      println(s"Estimated memory size: $memorySize bytes")
      println("Whole tree node number: " + tree.hen.size)
      println("non leaf node: " +  tree.non_leaf_node.size)
      println("leaf_node number: " + tree.leafNodeArrayBuffer.size)
      fw.close()


      // println("--Start to receive stream data--")
      // val streamData = List("GOOGLE:100", "FB:5", "AAPL:15")
      // streamData.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))
      // println(tree.hen(generateID("AAPL>13^GOOGLE>20^FB<15")).parents)
      // val query_set_url = readLine("Please enter target query, and url")
      // val target_query = query_set_url.split(",").head
      // val set_url = query_set_url.split(",").last

      // val streamData2 = readLine("Please enter stream data").split(',')
      // tree.hen(generateID(target_query)).setUrl(set_url)
      // streamData2.foreach( x => tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble))


    } 
  } catch {
  case ex: Exception =>
    recordErrorToFile(ex, "/home/pcdm/hann/Kafka_Regression/Loggg/error.log")
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
