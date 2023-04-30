import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._


trait  Node {
    val expression: String
    var parent: ListBuffer[Node] 
    var childs : ListBuffer[Node] 
    var useCount: Int
    var childExprs: ListBuffer[List[String]] 
    var trueCounter: Int
    
}

case class inner_Node(_expression: String) extends Node {
  val expression: String = _expression
  var parent: ListBuffer[Node] = ListBuffer[Node]()
  var childs: ListBuffer[Node] = ListBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]]  = ListBuffer[List[String]] ()
  var result: Boolean = false
  var trueCounter = 0
  
  

}

case class leaf_Node(_expression: String) extends Node {
  val expression: String = _expression
  var parent: ListBuffer[Node] = ListBuffer[Node]()
  var childs: ListBuffer[Node] = ListBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]]  = ListBuffer[List[String]] ()
  var result: Boolean = false
  var trueCounter = 0

  def receiveResult(result: Boolean): Unit = {
    if (this.getClass() == leaf_Node.getClass()) {
      this.result = result
      this.trueCounter = this.trueCounter + 1
      
    }
  }
}
val a = new leaf_Node("a")

println(a.isInstanceOf[leaf_Node])