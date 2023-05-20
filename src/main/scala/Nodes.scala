import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.P
import scalaj.http._
import java.io.Serializable


trait Node extends Product with  Serializable{

  val expression: String 
  var parents: ListBuffer[Node] 
  var childs: ListBuffer[Node] 
  var useCount: Int 
  var childExprs: ListBuffer[List[String]]
  var trueCounter: Int 
  var state: Boolean 
  var url:String 
  def receiveResult(result: Boolean,value: String, comesFrom: String): Unit 
  def setUrl(url:String):Unit 
  def receiveUpdateValue(value: String): Unit
  //def this() = this("",0L)
}

case class Switch_Node(_name: String, val targetNodes: Array[Leaf_Node]) extends Serializable {
  var true_false_Map: Map[Int, Array[Array[Leaf_Node]]] = Map.empty
  def sortedWithExpression(unsorted_Nodes: Array[Leaf_Node]): Array[Leaf_Node] = {
    val sortedWithExpression = unsorted_Nodes.sortBy{ node => val operandIdx = node.expression.indexWhere(Set('>','<','=').contains)
    node.expression.substring(operandIdx+1).toInt  
    }
    sortedWithExpression
    
  }
  def createTrue_False_Map(unsortedNodes: Array[Leaf_Node]):  Map[Int, Array[Array[Leaf_Node]]] = {
    val sortedNodes = sortedWithExpression(unsortedNodes)
    println(sortedNodes.length)
    for (i <- 0 until sortedNodes.length) {
      //println("i" + i)
      val trueNode = sortedNodes.slice(0, i+1).filter(x => x.expression.contains('>')).concat( sortedNodes.slice(i+1, sortedNodes.length).filter(x => x.expression.contains('<')))
      val falseNode = sortedNodes.slice(0, i+1).filter(x => x.expression.contains('<')).concat( sortedNodes.slice(i+1, sortedNodes.length).filter(x => x.expression.contains('>')))
      true_false_Map += (i -> Array(trueNode, falseNode))
    }
    true_false_Map += (-1 -> Array(sortedNodes.filter(x => x.expression.contains('<')), sortedNodes.filter(x => x.expression.contains('>'))) )
    
    true_false_Map
  }

  def receiveValueThenForward(comingValue: Double): Unit = { //Array[Array[Leaf_Node]]
    binarySearchInNodes(sortedWithExpression(targetNodes), comingValue) match {
      case -1 => {
        for(index <- 0 to 1) {
          if (index ==0) {
            true_false_Map(-1)(index).map(x => x.receiveResult(true, comingValue.toString()))
          } else {
            true_false_Map(-1)(index).map(x => x.receiveResult(false, comingValue.toString()))
          }
        }
      
      }//.map(x => x(0).receiveResult(true)) ; true_false_Map(-1).map(x => x(1).receiveResult(false))
      case i => {
         for(index <- 0 to 1) {
          if (index ==0) {
            true_false_Map(i)(index).map(x => x.receiveResult(true, comingValue.toString()))
          } else {
            true_false_Map(i)(index).map(x => x.receiveResult(false, comingValue.toString()))
          }
        }
      }//true_false_Map(i) //true_false_Map(i).map(x => x(1).receiveResult(false))
    }
  }

  def binarySearchInNodes(unsortedNodes: Array[Leaf_Node], target: Double): Int = {
    val sortedNodes = sortedWithExpression(unsortedNodes)
    // for (node <- sortedNodes) {
    //   if (node.expression.contains(target) && node.expression.contains('>')) {
    //     return sortedNodes.indexOf(node) - 1
    //     } else if (node.expression.contains(target) && node.expression.contains('<')) {
    //     return sortedNodes.indexOf(node) + 1
    //     } 
    //   }
    val onlyNumberArray = sortedNodes.map(x => x.expression.substring(x.expression.indexWhere(Set('>','<','=').contains)+1).toDouble)
    var final_index = -1
    if (onlyNumberArray.contains(target)) {
      val semi_index = onlyNumberArray.indexOf(target)
      if (sortedNodes(semi_index).expression.contains('>')) {
        final_index = semi_index - 1
      } else if (sortedNodes(semi_index).expression.contains('<')) {
        final_index = semi_index//+ 1
      }
    } else {
      final_index = findIndex(onlyNumberArray, target)
    }

    

    def findIndex(arr: Array[Double], value: Double): Int = {
      var left = 0
      var right = arr.length - 1
      var result = -1
    
      while (left <= right) {
        val mid = left + (right - left) / 2
        if (arr(mid) <= value) {
          result = mid
          left = mid + 1
        } else {
        right = mid - 1
        }
      }
      result
      }
    //println("Final index : +++" + final_index)

    final_index
  }



}


case class Inner_Node(val expression: String, var trueCounter:Int) extends Node{
  //val expression: String = _expression
  var parents: ListBuffer[Node] = ListBuffer[Node]()
  var childs: ListBuffer[Node] = ListBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  //var trueCounter = 0
  var url:String = ""
  var current_value: Map[String, String] = Map.empty
  
  var comedNode = ListBuffer[String]()
  def setUrl(url:String):Unit = {
    this.url = url
  }

  def receiveResult(result: Boolean, value:String, comesFrom: String): Unit = {
    println(expression + " receive result: " + result + " with value: " + value)
      // if (!comedNode.contains(comesFrom)) {
      
        // comedNode += comesFrom
      //println(expression + " comed Node: " + comesFrom)
      // if (comedNode.contains(comesFrom)) {
      //   println("comes from or not: " + comedNode.contains(comesFrom))
      //   if (result == true) {
      //     current_value += (value.split(":")(0) -> value.split(":")(1))
      //     sendUpdateValue(value)
      //   } else {
      //     if (trueCounter > 0 && state != result) {
      //       trueCounter -= 1
      //     } else {
      //       trueCounter -= 0
      // }

      //   }
      // } else {
        
      
      //println("comes from or not: " + comedNode.contains(comesFrom))
      //comedNode += comesFrom

      if (result == true ) { //&& state != result
        trueCounter += 1
        if (value.contains(",")) {
          val valueArray = value.split(",")
          for (v <- valueArray) {
            current_value += (v.split(":")(0) -> v.split(":")(1))
          }
        } else { 
          current_value += (value.split(":")(0) -> value.split(":")(1))
        }
      
      } else if (result == false && trueCounter > 0 ) { //&& state != result
        trueCounter -= 1
      } else {
        trueCounter -= 0
      }

    //}
      
      //println(expression + " now trueCounter: " + trueCounter + " with value: " + value)
      var newState = false
      

      if (trueCounter != childs.size) {
        newState = false
        // val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(", ")
        // propagateResult(newState, current_price)
      } else if (trueCounter == childs.size){
        newState = true
        val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(", ")
        // propagateResult(newState, current_price)
        // sendNotification()
        trueCounter = 0
        //comedNode.clear()
      }

      if (state != newState) {
        state = newState
        val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(", ")
        propagateResult(newState, current_price)
        comedNode = ListBuffer[String]()
        if (state == true) {
          sendNotification()
          state == false
          comedNode = ListBuffer[String]()
        }
      } else if (state == true && newState == true) {
        val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(", ")
        propagateResult(newState, current_price)
        comedNode = ListBuffer[String]()
      }
      else {
        sendUpdateValue(value)
      }
    
    //println(expression + "count " + trueCounter)
      // } else {
      // receiveUpdateValue(value)
      // }
}

  def receiveUpdateValue(value: String): Unit = {
    //trueCounter +=1
    if (value.contains(",")) {
      val valueArray = value.split(",")
      for (v <- valueArray) {
        current_value += (v.split(":")(0) -> v.split(":")(1))
      }
    } else { 
      current_value += (value.split(":")(0) -> value.split(":")(1))
    }
    val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(",")
    //propagateResult(state, current_price)
    // if (trueCounter == childs.size) {
    //   val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(",")
    //   propagateResult(state, current_price)
    //   trueCounter = 0
    // }
  }

  def sendUpdateValue(value: String): Unit = {
    parents.foreach(_.receiveUpdateValue(expression.split("[<>]").head + ":" + value))
  }




  def sendNotification(): Unit = {
    if (url != "" ) {
      val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(", ")
      println("current_price" + current_price)
      val testRequest = Http(url).postForm(Seq("value1" -> expression, "value2" -> s"Current Price: $current_price" ,"value3" -> "---")).asString
      println("Notification sent <- from" + this.expression)
      
    }
  }




  def propagateResult(result: Boolean, value:String , sendsFrom: String = expression): Unit = {
    parents.foreach(_.receiveResult(result, value, sendsFrom))
    // if (state == true) {
    //   state = false
    // }
    
  }

}

case class Leaf_Node(val expression: String) extends Node {
  //val expression: String = _expression
  var parents: ListBuffer[Node] = ListBuffer[Node]()
  var childs: ListBuffer[Node] = ListBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  var trueCounter = 0
  var url:String = ""

  //var value:String = ""
  def receiveResult(new_state: Boolean, value:String , sendsFrom:String = ""): Unit = {
    //println(expression + " receive result: " + new_state)
      
        println(expression + " receive result: " + new_state + " with value: " + value)
        state = new_state
        propagateResult(new_state, value)
      
      // if (state == true && new_state == true) {
        
      //   updateValue(value)
      // } else if (state != new_state) {
      //   println(expression + " receive result: " + new_state + " with value: " + value)
      //   state = new_state
      //   propagateResult(new_state, value)
      // }
      // if(state != new_state) {
      //   state = new_state
      //   propagateResult(new_state, value)
      //   //state = false
      // } else if (state == true && new_state == true){
      //   updateValue(value)
      // }
      //propagateResult(new_state, value)
      //println(expression + " receive result: " + new_state)
    
  }

  def propagateResult(result: Boolean, value:String, sendsFrom: String = expression): Unit = {
    parents.foreach(_.receiveResult(result, expression.split("[<>]").head + ":" + value, sendsFrom))
    // if (state == true) {
    //   state = false
    // } else {
    //   state = true
    // }
  }
  def updateValue(value:String): Unit = {
    parents.foreach(_.receiveUpdateValue(expression.split("[<>]").head + ":" + value))
  }


  def setUrl(url: String): Unit = {}
  def receiveUpdateValue(value: String): Unit ={}
}

case class ATree(name:String) extends Serializable{

  var hen: HashMap[Long, Node] = HashMap[Long, Node]()
  var root: ListBuffer[Node] = ListBuffer[Node]()
  var leafNodeArrayBuffer: ArrayBuffer[Node] = ArrayBuffer[Node]()
  var groupBySource_Map: Map[String,ArrayBuffer[Node]] = Map[String,ArrayBuffer[Node]]()
  var switch_Node_Map = Map[String, Switch_Node]()


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
              if (!s.contains("Seq")) {
              val childNode = insert(s)
              childNodes += childNode
              } else if (s.contains("Seq")) {
                val expr = s.substring(4, s.length() - 1)
                var predicates = expr.split(",")
                val exprwithand  = predicates.mkString("^")
                val childNode = insert(exprwithand) 
                childNodes += childNode
              }
            }
          } else if (childExprs(0).contains("Seq")) {
            val expr = childExprs(0).substring(4, childExprs(0).length() - 1)
            var predicates = expr.split(",")
            val exprwithand  = predicates.mkString("^")
            val childNode = insert(exprwithand) 
            childNodes += childNode
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
      new Leaf_Node(expr)
    } else {
      new Inner_Node(expr,0)
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
    target.foreach(x => if (!x._2.expression.contains('^')) leafNodeArrayBuffer += x._2)
  }

  def create_Switch_Node_from_groupbySource_Map(source: String, target: Array[Node]): Unit = {
    val name = source
    val target1 = target
    val target2 = ArrayBuffer[Leaf_Node]()
    for (node <- target1) {
      if (node.isInstanceOf[Leaf_Node]) {
        target2 += node.asInstanceOf[Leaf_Node]
      }
    } 
    val switch_Node_final = new Switch_Node(name, target2.toArray)
    switch_Node_final.createTrue_False_Map(target2.toArray)
    switch_Node_Map += (source -> switch_Node_final)
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
  //tree.insert("BTC>3^ETH>9^DOGE>10")
  //tree.insert("BTC>3^ETH>9^SOL>20")
  tree.insert("Seq(A>3,B<2,C>3)^ETH>9")
  tree.insert("A>3^B<2^C>3^SOL>20")
  tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
  tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
  println("-----------------------")
  tree.leafNodeArrayBuffer.foreach(x => println(x.expression))
  val regex = new Regex("([A-Za-z]+)[<>]\\d+")
  val groupMap = tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<'))
  val groupMap2 = groupMap.map(x => (x._1, x._2.map(_.expression)))
  println(groupMap2)
  println("-----------------------")
  tree.hen.foreach(x => println(x._1))
  tree.hen.foreach(x => println(x._2.expression))
  println(s"Seq(A>3,B<2,C>3)^ETH>9 childs: ${tree.hen(generateID("Seq(A>3,B<2,C>3)^ETH>9")).childs.map(_.expression)}")
  println(s"Seq(A>3,B<2,C>3)  childs: ${tree.hen(generateID("Seq(A>3,B<2,C>3)")).childs.map(_.expression)}")
  println(s"A^B^C childs: ${tree.hen(generateID("A>3^B<2^C>3")).childs.map(_.expression)}")
  println(s"A^B^C parents: ${tree.hen(generateID("A>3^B<2^C>3")).parents.map(_.expression)}")
  println(s"A>3^B<2^C>3^SOL>20 childs: ${tree.hen(generateID("A>3^B<2^C>3^SOL>20")).childs.map(_.expression)}")
  // println( s"Node BTC>3^ETH>9^DOGE>10's parents: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).parents.map(_.expression)}")
  // println( s"Node BTC>3^ETH>9^DOGE>10's childs: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).childs.map(_.expression)}")
  // println( s"Node BTC>3^ETH>9^SOL>20's parents: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).parents.map(_.expression)}")
  // println( s"Node BTC>3^ETH>9^SOL>20's childs: ${tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).childs.map(_.expression)}")
  // println( s"Node BTC>3^ETH>9's parents: ${tree.hen(generateID("BTC>3^ETH>9")).parents.map(_.expression)}")
  // println( s"Node BTC>3^ETH>9's parents: ${tree.hen(generateID("BTC>3^ETH>9")).parents.size}")
  // println( s"Node BTC>3^ETH>9's childs: ${tree.hen(generateID("BTC>3^ETH>9")).childs.map(_.expression)}")
  // tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).setUrl("https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  // println("\n")
  // println(s"Node(BTC>3) receive result: true")
  // tree.hen(generateID("BTC>3")).receiveResult(true)
  // println(tree.hen(generateID("BTC>3")).state)
  // println(tree.hen(generateID("BTC>3")).getClass())
  // println("\n")
  // println(s"After Node(BTC>3) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  // println(tree.hen(generateID("BTC>3^ETH>9")).state)
  // println("\n")
  // println(s"Node(ETH>9) receive result: true")
  // tree.hen(generateID("ETH>9")).receiveResult(true)
  // println(tree.hen(generateID("ETH>9")).state)
  // println(s"After Node(ETH>9) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  // println(tree.hen(generateID("BTC>3^ETH>9")).state)
  // println("\n")
  // println(s"After Node(DOGE>10) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  // tree.hen(generateID("DOGE>10")).receiveResult(true)
  // println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  // println(s"After Node(SOL>20) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  // tree.hen(generateID("SOL>20")).receiveResult(true)
  // println(tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  // println("---------TEST END----------")

  // println(s"Let's check Node(BTC>3^ETH>9^DOGE>10)'s state")
  // println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  // println(s"Let's check Node(BTC>3^ETH>9^SOL>20)'s state")
  // println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)  
  // println("\n")
  // println(s"let Node(ETH>9) receive result: false then check Node(BTC>3^ETH>9)'s state")
  // tree.hen(generateID("ETH>9")).receiveResult(false)
  // println(tree.hen(generateID("BTC>3^ETH>9")).state)
  // println("\n")
  // println(s"After Node(ETH>9) receive result: true then let's check Node(BTC>3^ETH>9) 's state")
  // tree.hen(generateID("ETH>9")).receiveResult(true)
  // println(tree.hen(generateID("BTC>3^ETH>9")).state)
  // println("\n")

  // println(s"let Node(DOGE>10) receive result: true then check Node(BTC>3^ETH>9DOGE>10)'s and Node(BTC>3^ETH>9SOL>20)'s state")
  // tree.hen(generateID("DOGE>10")).receiveResult(true)
  // println("Node(BTC>3^ETH>9^DOGE>10).state: " + tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  // println("Node(BTC>3^ETH>9^SOL>20).state: " + tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  // println("\n")
  // println(s"let Node(SOL>20) receive result: true then check Node(BTC>3^ETH>9^DOGE>10)'s and Node(BTC>3^ETH>9^SOL>20)'s state")
  // tree.hen(generateID("SOL>20")).receiveResult(true)
  // println("Node(BTC>3^ETH>9^DOGE>10).state: " + tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  // println("Node(BTC>3^ETH>9^SOL>20).state: " + tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  // println("\n")
  // println(s"let Node(SOL>20) receive result: false then check Node(BTC>3^ETH>9^DOGE>10)'s and Node(BTC>3^ETH>9^SOL>20)'s state")
  // tree.hen(generateID("SOL>20")).receiveResult(false)
  // println("Node(BTC>3^ETH>9^DOGE>10).state: " + tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).state)
  // println("Node(BTC>3^ETH>9^SOL>20).state: " + tree.hen(generateID("BTC>3^ETH>9^SOL>20")).state)
  // println("Node(BTC>3^ETH>9).state: " + tree.hen(generateID("BTC>3^ETH>9")).state)
}
