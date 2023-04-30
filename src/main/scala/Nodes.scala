import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.P
import scalaj.http._
abstract class  Node extends Product{

  val expression: String
  var parents: ListBuffer[Node]
  var childs: ListBuffer[Node]
  var useCount: Int
  var childExprs: ListBuffer[List[String]]
  var trueCounter: Int
  var state: Boolean
  var url:String
  def receiveResult(result: Boolean): Unit
  def setUrl(url:String):Unit

}

case class inner_Node(_expression: String, var trueCounter:Int) extends Node{
  val expression: String = _expression
  var parents: ListBuffer[Node] = ListBuffer[Node]()
  var childs: ListBuffer[Node] = ListBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  //var trueCounter = 0
  var url:String = ""

  def setUrl(url:String):Unit = {
    this.url = url
  }

  def receiveResult(result: Boolean): Unit = {
    println(expression + "receive result: " + result)
    
    if (result == true) {
      trueCounter += 1
    } else if (result == false && trueCounter > 0) {
      trueCounter -= 1
    } else {
      trueCounter -= 0
    }
    var newState = false
    if (trueCounter != childs.size) {
      newState = false
    } else {
      newState = true
      
      trueCounter = 0
    }

    if (state != newState) {
      state = newState
      propagateResult(newState)
      if (state == true) {
        sendNotification()
      }
    } else {}
    println(expression + "count " + trueCounter)
  }
  def sendNotification(): Unit = {
    if (url != "" ) {
      val testRequest = Http(url).asString.body
      println("Notification sent <- from" + this.expression)
    }
  }




  def propagateResult(result: Boolean): Unit = {
    parents.foreach(_.receiveResult(result))
  }

}

case class leaf_Node(_expression: String) extends Node {
  val expression: String = _expression
  var parents: ListBuffer[Node] = ListBuffer[Node]()
  var childs: ListBuffer[Node] = ListBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  var trueCounter = 0
  var url:String = ""
  def receiveResult(result: Boolean): Unit = {
    if (this.isInstanceOf[leaf_Node]) {
      this.state = result
      propagateResult(result)
      println(expression + "receive result: " + result)
    }
  }

  def propagateResult(result: Boolean): Unit = {
    parents.foreach(_.receiveResult(result))
  }

  def setUrl(url: String): Unit = {}

}

case class ATree(name:String) {

  var hen: HashMap[Long, Node] = HashMap[Long, Node]()
  var root: ListBuffer[Node] = ListBuffer[Node]()
  var leafNodeArrayBuffer: ArrayBuffer[Node] = ArrayBuffer[Node]()
  var groupMap: Map[String,ArrayBuffer[Node]] = Map[String,ArrayBuffer[Node]]()
  def insert(_expression: String): Node = {
    val id = generateID(_expression)
    println(_expression + "   " + id)
    if (hen.getOrElse(id, 0) != 0) {
      hen(id).useCount += 1
      hen(id)
    } else {
      var childExprs = reorganize(_expression)
      var flag = true
      if (childExprs.isEmpty) {
        childExprs = List(_expression)
        flag = false
      }

      println("flag: " + flag)
      var childNodes: ListBuffer[Node] = ListBuffer[Node]()

      if (_expression.length() > 1) {
        if (childExprs.size > 1) {
          for (expr <- childExprs) {
            println("!!!!!!!!!!!!!!!!!!!expr: " + expr)
            var childNode = insert(expr)
            childNodes += childNode
          }
        } else {
          if (childExprs(0).contains('^')) {
            val expr = childExprs(0)
            var predicates = expr.split('^')
            for (s <- predicates) {
              val childNode = insert(s)
              childNodes += childNode
            }
          }

        }

      }
      val node = createNewNode(_expression, childNodes.toList)
      node.useCount += 1
      node.childExprs += childExprs

      if (flag == true) {
        selfAdjust(node)

      }

      hen += (id -> node)
      node
    }

  }

  private def createNewNode(expr: String, childNodes: List[Node]): Node = {
    val id = generateID(expr)
    val node = if (!expr.contains('^')) {
      new leaf_Node(expr)
    } else {
      new inner_Node(expr,0)
    }
    for (childNode <- childNodes) {
      node.childs += childNode
      childNode.parents += node
    }

    hen += (id -> node)
    node
  }

  private def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
    }

    id
  }

  private def reorganize(_expression: String): List[String] = {

    println("Reorganize: " + _expression)
    var u = ListBuffer(List(_expression))
      .map(_.flatMap(_.split("[\\^∨]")).toSet)
      .toSet

    var c: ListBuffer[String] = ListBuffer[String]()
    var round = 0
    println("U :" + u.mkString(", "))
    breakable {
      while (u.nonEmpty) {

        val s = selectAn_S_that_maximizes_insect_in_Hen(u, _expression)
        println("s: " + s)
        if (s.isEmpty) {

          break()
        }
        u = u.map(_.filter(!s.contains(_)))
        println("u: " + u)
        c += setRecoveryToStringWithAnd(s)
        println("c: " + c)
        round += 1
      }

    }
    if (round > 1) {}
    val foruni = u.map(x => x.toList).toList

    c.appendAll(foruni.flatten).toList

  }

  private def find_max_intersect(
      set1: Set[String],
      target: HashMap[Int, Node]
  ): Set[String] = {
    var maxinterSet: Set[String] = Set.empty
    for ((id, node) <- target) {
      val interSet = set1.intersect(exprToPredicateSet(node.expression))
      if (interSet.size > maxinterSet.size) {
        maxinterSet = interSet
      }
    }

    maxinterSet

  }

  def charSetToStringSet(set: Set[Char]): Set[String] = {
    var stringSet: Set[String] = Set.empty
    for (char <- set) {
      stringSet += char.toString()
    }
    stringSet
  }

  def stringSetToCharSet(set: Set[String]): Set[Char] = {
    var charSet: Set[Char] = Set.empty
    for (string <- set) {
      charSet += string.charAt(0)
    }
    charSet
  }

  def exprToPredicateSet(expr: String): Set[String] = {
    var stringSet: Set[String] = Set.empty
    println("expr :  " + expr)

    for (predicate <- expr.split('^')) {
      if (predicate.contains('∨')) {
        predicate.split('∨').foreach(stringSet += _)
      } else {
        stringSet += predicate
      }
    }
    stringSet

  }

  def setRecoveryToStringWithAnd(set: Set[String]): String = {
    var string = ""
    for (s <- set) {
      string += s + "^"
    }
    string = string.substring(0, string.length() - 1)
    string
  }

  def selectAn_S_that_maximizes_insect_in_Hen(
      target_set: Set[Set[String]],
      _expression: String
  ): Set[String] = {

    var maxinterSet: Set[String] = Set.empty
    val hen_no_self = hen - generateID(_expression)
    for (target <- target_set) {
      for ((id, node) <- hen_no_self) {
        val interSet = target.intersect(exprToPredicateSet(node.expression))
        if (interSet.size > maxinterSet.size) {
          maxinterSet = interSet
        }
      }
    }
    maxinterSet
  }

  def selfAdjust(newNode: Node): Unit = {
    var childNodes = newNode.childs

    for (i <- 0 until childNodes.size) {
      var j = 0
      while (j < childNodes(i).parents.size - 1) {

        if (
          parentExpressionContainsNewExpression(
            childNodes(i).parents(j).expression,
            newNode.expression
          )
        ) {

          childNodes(i).parents(j).childs -= childNodes(i)
          if (!childNodes(i).parents(j).childs.contains(newNode)) {
            childNodes(i).parents(j).childs += newNode
          }
          childNodes(i).parents -= childNodes(i).parents(j)
          if (
            !newNode.parents.contains(childNodes(i).parents(j)) && childNodes(i)
              .parents(j) != newNode
          ) {
            newNode.parents += childNodes(i).parents(j)
          }

        } else {
          j += 1
        }

      }

    }

  }

  def checkNodeChildsParent(node: Node) {
    for (child <- node.childs) {
      if (!child.parents.contains(node)) {
        child.parents += node
      }
    }
  }

  def parentExpressionContainsNewExpression(
      parentExpression: String,
      newExpression: String
  ): Boolean = {
    val parentSet = exprToPredicateSet(parentExpression)
    val newSet = exprToPredicateSet(newExpression)
    if (newSet.subsetOf(parentSet)) {
      true
    } else {
      false
    }
  }

  def from_hen_collect_leaf_Node_to_ArrayBuffer(target: HashMap[Long,Node], con: ArrayBuffer[Node]):Unit = {
    target.foreach(x => if (x._2.isInstanceOf[leaf_Node]) leafNodeArrayBuffer += x._2)
  }





}


object Nodes extends  App{
  def generateID(_expression: String): Long = {
    val predicatesAndOperators: List[Char] = _expression.toList
    var id: Int = 0
    for (char <- predicatesAndOperators) {
      id += char.hashCode() * char.hashCode()
    }

    id
  }

  val tree = new ATree("1")
  tree.insert("BTC>3^ETH>9^DOGE>10")
  tree.insert("BTC>3^ETH>9^SOL>20")
  tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
  tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
  println("-----------------------")
  tree.leafNodeArrayBuffer.foreach(x => println(x.expression))
  val regex = new Regex("([A-Za-z]+)[<>]\\d+")
  val groupMap = tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<'))
  val groupMap2 = groupMap.map(x => (x._1, x._2.map(_.expression)))
  println(groupMap2)
  println("-----------------------")
  // tree.hen.foreach(x => println(x._1))
  // tree.hen.foreach(x => println(x._2.expression))
  println( s"Node BTC>3^ETH>9^DOGE>10's parents: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).parents.map(_.expression)}")
  println( s"Node BTC>3^ETH>9^DOGE>10's childs: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).childs.map(_.expression)}")
  println( s"Node BTC>3^ETH>9^SOL>20's parents: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).parents.map(_.expression)}")
  println( s"Node BTC>3^ETH>9^SOL>20's childs: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).childs.map(_.expression)}")
  println( s"Node BTC>3^ETH>9's parents: ${tree.hen(generateID("BTC>3^ETH>9")).parents.map(_.expression)}")
  println( s"Node BTC>3^ETH>9's parents: ${tree.hen(generateID("BTC>3^ETH>9")).parents.size}")
  println( s"Node BTC>3^ETH>9's childs: ${tree.hen(generateID("BTC>3^ETH>9")).childs.map(_.expression)}")
  
  println("\n")
  println(s"Node(BTC>3) receive result: true")
  tree.hen(generateID("BTC>3")).receiveResult(true)
  println(tree.hen(generateID("BTC>3")).state)
  println("\n")
  println(s"After Node(BTC>3) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  println(tree.hen(generateID("BTC>3^ETH>9")).state)
  println("\n")
  println(s"Node(ETH>9) receive result: true")
  tree.hen(generateID("ETH>9")).receiveResult(true)
  println(tree.hen(generateID("ETH>9")).state)
  println(s"After Node(ETH>9) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  println(tree.hen(generateID("BTC>3^ETH>9")).state)
  println("\n")
  println(s"After Node(DOGE>10) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  tree.hen(generateID("DOGE>10")).receiveResult(true)
  println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  println(s"After Node(SOL>20) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  tree.hen(generateID("SOL>20")).receiveResult(true)
  println(tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  println("---------TEST END----------")

  println(s"Let's check Node(BTC>3^ETH>9^DOGE>10)'s state")
  println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  println(s"Let's check Node(BTC>3^ETH>9^SOL>20)'s state")
  println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)  
  println("\n")
  println(s"let Node(ETH>9) receive result: false then check Node(BTC>3^ETH>9)'s state")
  tree.hen(generateID("ETH>9")).receiveResult(false)
  println(tree.hen(generateID("BTC>3^ETH>9")).state)
  println("\n")
  println(s"After Node(ETH>9) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  tree.hen(generateID("ETH>9")).receiveResult(true)
  println(tree.hen(generateID("BTC>3^ETH>9")).state)
  println("\n")

  println(s"let Node(DOGE>10) receive result: true then check Node(BTC>3^ETH>9DOGE>10)'s and Node(BTC>3^ETH>9SOL>20)'s state")
  tree.hen(generateID("DOGE>10")).receiveResult(true)
  println("Node(BTC>3^ETH>9^DOGE>10).state: " + tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  println("Node(BTC>3^ETH>9^SOL>20).state: " + tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  println("\n")
  println(s"let Node(SOL>20) receive result: true then check Node(BTC>3^ETH>9^DOGE>10)'s and Node(BTC>3^ETH>9^SOL>20)'s state")
  tree.hen(generateID("SOL>20")).receiveResult(true)
  println("Node(BTC>3^ETH>9^DOGE>10).state: " + tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  println("Node(BTC>3^ETH>9^SOL>20).state: " + tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  println("\n")
  println(s"let Node(SOL>20) receive result: false then check Node(BTC>3^ETH>9^DOGE>10)'s and Node(BTC>3^ETH>9^SOL>20)'s state")
  tree.hen(generateID("SOL>20")).receiveResult(false)
  println("Node(BTC>3^ETH>9^DOGE>10).state: " + tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  println("Node(BTC>3^ETH>9^SOL>20).state: " + tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  println("Node(BTC>3^ETH>9).state: " + tree.hen(generateID("BTC>3^ETH>9")).state)
}
