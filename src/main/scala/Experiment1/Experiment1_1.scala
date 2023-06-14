import scala.util.matching.Regex
import org.apache.hadoop.shaded.org.checkerframework.checker.units.qual.s
import java.lang.management.ManagementFactory
import com.han.ATree
object Experiment1_1 {
  def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
    }

    id

  }
  def getObjectMemoryUsage(obj: AnyRef): Long = {
  val runtime = ManagementFactory.getMemoryMXBean
  val memoryUsage = runtime.getHeapMemoryUsage
  val sizeBefore = memoryUsage.getUsed

  // Create a reference to the object to prevent it from being garbage collected
  val reference = new Array[AnyRef](1)
  reference(0) = obj

  val sizeAfter = memoryUsage.getUsed
  val objectMemoryUsage = sizeAfter - sizeBefore

  // Clear the reference to the object
  reference(0) = null

  objectMemoryUsage
}

  def main(args: Array[String]): Unit = {
    val tree = new ATree("Experiment_1")
    val query1 = "P1^P2^P3"
    val query2 = "P4^P1^P2"
    val startTime = System.currentTimeMillis()
    tree.add_query(query1)
    tree.add_query(query2)
    
    tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
    tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
    val endTime = System.currentTimeMillis()
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

    println("P1^P2's childs: "  +tree.hen(generateID("P1^P2")).childs)

    println(s"Program Run Time: ${endTime - startTime} ms")
    val memoryUsage = getObjectMemoryUsage(tree)
    println(s"Memory usage of myObject: $memoryUsage bytes")

  }
}
