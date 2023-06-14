import scala.collection.mutable.ListBuffer
import com.han.ATree
import org.apache.spark.util.SizeEstimator
object Experiment_new2_1 {

  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()

// Generate 100 random queries and add them to query_set
    for (_ <- 1 to 500) {
      val randomQuery = GenerateQuery.generateQueryExpression()
      query_set += randomQuery
    }
    
    println(" ")
    // query_set.foreach(x => println(x))
    
    val tree = new ATree("Experiment_1")
    val tree_startTime = System.currentTimeMillis()
    query_set.foreach(x => tree.add_query(x))
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    val tree_endTime = System.currentTimeMillis()
    val switch_startTime = System.currentTimeMillis()
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
    tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
    val switch_endTime = System.currentTimeMillis()
    
    println(s"Program Construct Time: ${tree_startTime - tree_endTime} ms")
    println(s"Switch Construct Time: ${switch_startTime - switch_endTime} ms")
    val memorySize = SizeEstimator.estimate(tree)
    println(s"Estimated memory size: $memorySize bytes")
    println(tree.hen.size)
    
  }
}
