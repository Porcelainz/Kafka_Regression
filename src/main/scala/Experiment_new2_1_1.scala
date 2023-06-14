import scala.collection.mutable.ListBuffer

object Experiment_new2_1_1 {

  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()

// Generate 100 random queries and add them to query_set
    // for (_ <- 1 to 10) {
    //   val randomQuery = GenerateQuery.generateQueryExpression()
    //   query_set += randomQuery
    // }
    query_set += "BTC>10^ETH<11^SOL=100"
    query_set += "BTC>10^ETH<13^DOGE=50"
    query_set += "SNK<53^QUE=7^FILO>64"
    query_set += "SYN<40^ASK=10^SOL=100"
    query_set += "SLE<33^ASQE>32^DOGE>10"
    
    println("TEST")
    // query_set.foreach(x => println(x))
    val startTime = System.currentTimeMillis()
    val tree = new ATree("Experiment_1")
    query_set.foreach(x => tree.add_query(x))
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
    tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
    val endTime = System.currentTimeMillis()
    println(s"Program Run Time: ${endTime - startTime} ms")
  }
}
