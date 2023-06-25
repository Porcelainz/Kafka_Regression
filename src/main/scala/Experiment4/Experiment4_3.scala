package Experiment4
import scala.collection.mutable.ListBuffer
import com.han.ATree
import scala.util.hashing.MurmurHash3
object Experiment4_3 {
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
  def main(args: Array[String]): Unit = {
    val query_set = ListBuffer[String]()
    val q1 = "BTC>10^SOL<20^FTT>5"
    query_set += q1 

    val tree = new ATree("Experiment_4")
    query_set.foreach(x => tree.add_query(x))
    tree.hen(generateID(q1)).setUrl("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM")
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<').takeWhile(_ != '='))
    tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
    println(tree.hen(generateID(q1)).childs)

    val btc_range = (5, 15)
    val sol_range = (10, 25)
    val ftt_range = (1, 10)
    val source_candidates = Array("BTC", "SOL", "FTT")
    def generate_random_stream_value(): String = {
      val source = source_candidates(scala.util.Random.nextInt(source_candidates.length))
      var value = 0
      if (source == "BTC") {
        value = scala.util.Random.between(btc_range._1, btc_range._2) 
        //s"$source:$value"
      } else if (source == "SOL") {
       value = scala.util.Random.between(sol_range._1,  sol_range._2) 
        //s"$source:$value"
      } else {
        value = scala.util.Random.between(ftt_range._1, ftt_range._2)
        //s"$source:$value"
      }
      s"$source:$value"
    }
    //println(generate_random_stream_value() )



    //val inputValues = Array("BTC:11", "SOL:11", "FTT:12","BTC:22", "BTC:-1", "BTC:22", "SOL:22", "FTT:22", "BTC:33", "SOL:33", "FTT:33") 
    //val event_happen_time = System.currentTimeMillis() 

    for(i <- 0 to 100) {
      val inputValue = generate_random_stream_value()
      tree.switch_Node_Map(inputValue.split(":").head).receiveValueThenForward(inputValue.split(":").last.toDouble)
      Thread.sleep(1000)
    }
    //inputValues.foreach( x =>  {tree.switch_Node_Map(x.split(":").head).receiveValueThenForward(x.split(":").last.toDouble) ; Thread.sleep(5000)})
    // for (i <- 0 until(inputValues.length)) {
    //   tree.switch_Node_Map(inputValues(i).split(":").head).receiveValueThenForward(inputValues(i).split(":").last.toDouble)
    //   //Thread.sleep(5000)
    // }
    // val trigger_time = System.currentTimeMillis()
    // println(s"Latency : ${trigger_time - event_happen_time} ms")
  
  }
}