package com.han
import scala.util.hashing.MurmurHash3
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.P
import scalaj.http._
import java.io.Serializable
import breeze.numerics.exp
import os.remove
import spire.std.array
import scala.collection.parallel.CollectionConverters._
import scala.collection.mutable
import scala.collection.parallel.ParSeq
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Failure
import scala.util.Success

trait Node extends Product with Serializable {

  val expression: String
  var parents: ArrayBuffer[Node]
  var childs: ArrayBuffer[Node]
  var useCount: Int
  var childExprs: ListBuffer[List[String]]
  var trueCounter: Int
  var state: Boolean
  var url: String
  def receiveResult(result: Boolean, value: String, comesFrom: Node): Unit
  def setUrl(url: String): Unit
  def receiveUpdateValue(value: String): Unit
  // def this() = this("",0L)
}

case class Switch_Node(_name: String, val targetNodes: Array[Leaf_Node])
    extends Serializable {
  var bigger_true_false_Map: Map[Int, Array[Array[Leaf_Node]]] = Map.empty
  var smaller_true_false_Map: Map[Int, Array[Array[Leaf_Node]]] = Map.empty
  var equal_symbol_Map: Map[Double, Leaf_Node] = Map.empty
  def sortedWithExpression(
      unsorted_Nodes: Array[Leaf_Node]
  ): Array[Leaf_Node] = {
    val sortedWithExpression = unsorted_Nodes.sortBy { node =>
      val operandIdx = node.expression.indexWhere(Set('>', '<','=').contains)
      node.expression.substring(operandIdx + 1).toDouble
    }
    sortedWithExpression

  }

  def create_equal_symbol_Map(unsortedNodes: Array[Leaf_Node]): Map[Double, Leaf_Node] = {
    val sortedNodes = sortedWithExpression(unsortedNodes)
    for (node <- sortedNodes) {
      if (node.expression.contains("=")) {
        equal_symbol_Map += (node.expression.split("=").last.toDouble -> node)
      }
      
    }
    //println(equal_symbol_Map)
    equal_symbol_Map
  }


  def createTrue_False_Map(
      unsortedNodes: Array[Leaf_Node]
  ): Unit/*Map[Int, Array[Array[Leaf_Node]]]*/ = {
    val sortedNodes = sortedWithExpression(unsortedNodes)
    //println("sorted Nodes : "+sortedNodes.mkString(", "))
    // for (i <- 0 until sortedNodes.length) {
    //   // println("i" + i)
    //   val trueNode = sortedNodes
    //     .slice(0, i + 1)
    //     .filter(x => x.expression.contains('>'))
    //     .concat(
    //       sortedNodes
    //         .slice(i + 1, sortedNodes.length)
    //         .filter(x => x.expression.contains('<'))
    //     )
    //   val falseNode = sortedNodes
    //     .slice(0, i + 1)
    //     .filter(x => x.expression.contains('<'))
    //     .concat(
    //       sortedNodes
    //         .slice(i + 1, sortedNodes.length)
    //         .filter(x => x.expression.contains('>'))
    //     )
    //   true_false_Map += (i -> Array(trueNode, falseNode))
    // }
    // true_false_Map += (-1 -> Array(
    //   sortedNodes.filter(x => x.expression.contains('<')),
    //   sortedNodes.filter(x => x.expression.contains('>'))
    // ))
    // //true_false_Map.foreach(x => println(x._1 + " " + "True Node: "+x._2(0).mkString(",") + " " + "False Node: "+ x._2(1).mkString(",")))
    // true_false_Map
    val for_smaller = sortedNodes.filter(x => x.expression.contains('<'))
    for (i <- 0 until for_smaller.length) {
      val trueNode = for_smaller.slice(i+1, for_smaller.length)
      val falseNode = for_smaller.slice(0, i+1)
      smaller_true_false_Map += (i -> Array(trueNode, falseNode))
    }
    smaller_true_false_Map += (-1 -> Array(
      for_smaller.slice(0, for_smaller.length),
      Array.empty[Leaf_Node]
    ))
    val for_bigger = sortedNodes.filter(x => x.expression.contains('>'))
    for (i <- 0 until for_bigger.length) {
      val trueNode = for_bigger.slice(0, i+1)
      val falseNode = for_bigger.slice(i+1, for_bigger.length)
      bigger_true_false_Map += (i -> Array(trueNode, falseNode))
    }
    bigger_true_false_Map += (-1 -> Array(
      Array.empty[Leaf_Node],
      for_bigger.slice(0, for_bigger.length)
    ))
  
  }

  def receiveValueThenForward(comingValue: Double): Unit = { // Array[Array[Leaf_Node]]
    val switch_receive_time = System.currentTimeMillis()
    val for_smaller_index = binarySearchInNodes_for_small(sortedWithExpression(targetNodes).filter(x => x.expression.contains('<')), comingValue)
    // for smaller 
    for_smaller_index match {
      case -1 => {
         for (index <- 0 to 1) {
          if (index == 0) {
            smaller_true_false_Map(-1)(index).map(x =>
              x.receiveResult(true, comingValue.toString())
            )
            
            
          } else {
            smaller_true_false_Map(-1)(index).map(x =>
              x.receiveResult(false, comingValue.toString())
            )
            
          }
        }
      } case i => {
        for (index <- 0 to 1) {
          if (index == 0) {
            if (smaller_true_false_Map.contains(i)) {
            smaller_true_false_Map(i)(index).map(x =>
              x.receiveResult(true, comingValue.toString())
            )
            }
            
          } else {
            if (smaller_true_false_Map.contains(i)) {
            smaller_true_false_Map(i)(index).map(x =>
              x.receiveResult(false, comingValue.toString())
            )
            }
            
          }
        }
      }
    }
    // for bigger
    val for_bigger_index = binarySearchInNodes_for_big(sortedWithExpression(targetNodes).filter(x => x.expression.contains('>')), comingValue)
    for_bigger_index match {
      case -1 => {
         for (index <- 0 to 1) {
          if (index == 0) {
            bigger_true_false_Map(-1)(index).map(x =>
              x.receiveResult(true, comingValue.toString())
            )
            
            
          } else {
            bigger_true_false_Map(-1)(index).map(x =>
              x.receiveResult(false, comingValue.toString())
            )
            
          }
        }
      } case i => {
        for (index <- 0 to 1) {
          if (index == 0) {
            if (bigger_true_false_Map.contains(i)) {
              bigger_true_false_Map(i)(index).map(x =>
                x.receiveResult(true, comingValue.toString())
              )
            }
            
            
          } else {
            if (bigger_true_false_Map.contains(i)) {
              bigger_true_false_Map(i)(index).map(x =>
                x.receiveResult(false, comingValue.toString())
              )
            }
            
            
          }
        }
      }
    }
    //println("why not come here ?" )
    //println(equal_symbol_Map)
    if (equal_symbol_Map.contains(comingValue)) {
      equal_symbol_Map(comingValue).receiveResult(true, comingValue.toString())
    } else {
      equal_symbol_Map.values.foreach(x => x.receiveResult(false, comingValue.toString()))
    }
    //val switch_send_out_time = System.currentTimeMillis()
    //println("Switch processing time: " + (switch_send_out_time - switch_receive_time) + " ms")
  }

  def binarySearchInNodes_for_small(
      unsortedNodes: Array[Leaf_Node],
      target: Double
  ): Int = {
    val sortedNodes = sortedWithExpression(unsortedNodes)
    
    val onlyNumberArray = sortedNodes.map(x =>
      x.expression
        .substring(x.expression.indexWhere(Set('>', '<', '=').contains) + 1)
        .toDouble
    ).distinct
 
    def findIndex(arr: Array[Double], value: Double):Int = {
      
      var result = -1
      if (arr.length == 1) {
        //println(arr.mkString(","))
        if (arr(0) == value) {
          result = 0
          
        } else if ( arr(0) < value) {
          result = 0
        }
      } else {
        //println(arr.mkString(","))
        for (i <- 0 until arr.length) {
          if (arr(i) < value) {
            result = i
          
        }
      }
      }
      result
    }
    
    val final_index = findIndex(onlyNumberArray, target)
    println("Final index : " + final_index + " From " + target)
    //final_index
    final_index
  }


  def binarySearchInNodes_for_big(
      unsortedNodes: Array[Leaf_Node],
      target: Double
  ): Int = {
    val sortedNodes = sortedWithExpression(unsortedNodes)
    
    val onlyNumberArray = sortedNodes.map(x =>
      x.expression
        .substring(x.expression.indexWhere(Set('>', '<', '=').contains) + 1)
        .toDouble
    ).distinct
 
    def findIndex(arr: Array[Double], value: Double):Int = {
      
      var result = -1
      if (arr.length == 1) {
        //println(arr.mkString(","))
        if (arr(0) == value) {
          result = -1
          
        } else if ( arr(0) < value) {
          result = 0
        }
      } else {
        //println(arr.mkString(","))
        for (i <- 0 until arr.length) {
          if (arr(i) < value) {
            result = i
          
        }
      }
      }
      result
    }
    
    val final_index = findIndex(onlyNumberArray, target)
    println("Final index : " + final_index + " From " + target)
    //final_index
    final_index
  }


  def binarySearchInNodes(
      unsortedNodes: Array[Leaf_Node],
      target: Double
  ): Int = {
    val sortedNodes = sortedWithExpression(unsortedNodes)
    
    val onlyNumberArray = sortedNodes.map(x =>
      x.expression
        .substring(x.expression.indexWhere(Set('>', '<', '=').contains) + 1)
        .toDouble
    ).distinct
 
    def findIndex(arr: Array[Double], value: Double):Int = {
      
      var result = -1
      if (arr.length == 1) {
        //println(arr.mkString(","))
        if (arr(0) == value) {
          result = 0
          
        } else if ( arr(0) < value) {
          result = 0
        }
      } else {
        //println(arr.mkString(","))
        for (i <- 0 until arr.length) {
          if (arr(i) < value) {
            result = i
          
        }
      }
      }
      result
    }
    
    val final_index = findIndex(onlyNumberArray, target)
    println("Final index : " + final_index + " From " + target)
    //final_index
    final_index
  }

}

case class Inner_Node(val expression: String, var trueCounter: Int)
    extends Node {
  // val expression: String = _expression
  var parents: ArrayBuffer[Node] = ArrayBuffer[Node]()
  var childs:ArrayBuffer[Node] = ArrayBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  // var trueCounter = 0
  var url: String = ""
  var current_value: Map[String, String] = Map.empty

  var comed_Node_Buffer = ListBuffer[Node]()
  //var comed_Node_Buffer_With_Time = ListBuffer[(Node, Long)]()
  def setUrl(url: String): Unit = {
    this.url = url
  }

  def receiveResult(comes_state: Boolean,value: String,comesFrom: Node): Unit = {
    println(expression +" Receive result " + comes_state + " from " + comesFrom.expression)
    def check_state(): Boolean = {
      if (comed_Node_Buffer.size == childs.size) {
        true
      } else {
        false
      }
    }
    // def check_state_Seq(): Boolean = {
    //   if (comed_Node_Buffer.size == childs.size) {
    //     val isOrdered: Boolean = comed_Node_Buffer_With_Time.zip(comed_Node_Buffer_With_Time.tail)
    //     .forall { case ((_, time1), (_, time2)) => time1 < time2 }
    //     isOrdered
    //   } else {
    //     false
    //   }
      
      
    // }
    // def removeFromTimeBuffer(target_node: Node): Unit = {
    //     val indexToRemove: Int = comed_Node_Buffer_With_Time.indexWhere { case (node, _) => node == target_node }
    //     comed_Node_Buffer_With_Time.remove(indexToRemove)
    //   }
    var new_state = false
    // if (expression.contains("Seq")) {
    //   if (comes_state == true && comed_Node_Buffer.contains(comesFrom) == false) {
    //     comed_Node_Buffer += comesFrom
    //     comed_Node_Buffer_With_Time += ((comesFrom, System.currentTimeMillis()))
    //     new_state = check_state_Seq()
    //     if (new_state) {
    //       if (url != "") {
    //         // sendNotification()
    //         println("True trigger true action send http request to" + url)
    //       }
    //       state = new_state
    //       comed_Node_Buffer.clear()
    //       // println("True trigger true action send http request to"+ url)
    //     } else if (state == true && new_state == false) {
    //       if (url != "") {
    //         println(
    //           "state turn from TRUE TO FALSE" + " False trigger true action send http request to" + url
    //         )
    //       }
    //       state = new_state
    //     }
    //   } else if (comes_state == false && comed_Node_Buffer.contains(comesFrom) == true) {
    //     comed_Node_Buffer -= comesFrom
    //     removeFromTimeBuffer(comesFrom)
    //     new_state = check_state()
    //     state = new_state
    //   }
    //   //state = new_state
      
    //   if (!parents.isEmpty) {
    //     propagateResult(state, value)
    //   }
    //}
     //else {
      if (comes_state == true && comed_Node_Buffer.contains(comesFrom) == false) {
        //println("Into 1")
        comed_Node_Buffer += comesFrom
        //comed_Node_Buffer_With_Time += ((comesFrom, System.currentTimeMillis()))
        new_state = check_state()
        if (new_state) {
          if (url != "") {
            val f : Future[Unit] = Future {sendNotification()}
            val result: Unit = Await.result(f, 5.seconds)
            f.onComplete {
              case Success(_) =>
                //println("Notification sent successfully.")
              case Failure(exception) =>
                //println(s"Failed to send notification: ${exception.getMessage}")
            }
            //println("True trigger true action send http request to" + url)
          }
          state = new_state
          propagateResult(state, value)
          comed_Node_Buffer.clear()
          // println("True trigger true action send http request to"+ url)
        } else if (state == true && new_state == false) {
          if (url != "") {
            println(
              "state turn from TRUE TO FALSE" + " False trigger true action send http request to" + url
            )
          }
          state = new_state
          propagateResult(state, value)
        }
      } else if (comes_state == false && comed_Node_Buffer.contains(comesFrom) == true) {
        //println("Into 2")
        comed_Node_Buffer -= comesFrom
        //removeFromTimeBuffer(comesFrom)
        new_state = check_state()
        if (state == true && new_state == false) {
          if (url != "") {
            println(
              "state turn from TRUE TO FALSE" + " False trigger true action send http request to" + url
            )
          }
        }
        
        state = new_state
        propagateResult(state, value)
      } else if (comes_state == false && comed_Node_Buffer.contains(comesFrom) == false) {
        //println("Into 3")
        //comed_Node_Buffer += comesFrom
        //comed_Node_Buffer_With_Time += ((comesFrom, System.currentTimeMillis()))
        new_state = false
        if (state == true && new_state == false) {
          if (url != "") {
            println(
              "state turn from TRUE TO FALSE" + " False trigger true action send http request to" + url
            )
          }
        }
        state = new_state
        propagateResult(state, value)
      } 
      
      //state = new_state
      
      
        
      
    //}
    
    // if (new_state) {
    //   if (url != "") {
    //     //sendNotification()
    //     println("True trigger true action send http request to"+ url)
    //   }
    //   comed_Node_Buffer.clear()
    //   //println("True trigger true action send http request to"+ url)
    // } else if (state == true && new_state == false){
    //   if (url != "") {
    //     println("state turn from TRUE TO FALSE" +" False trigger true action send http request to"+ url)
    //   }

    // }
    //state = new_state

  }

  def receiveUpdateValue(value: String): Unit = {
    // trueCounter +=1
    if (value.contains(",")) {
      val valueArray = value.split(",")
      for (v <- valueArray) {
        current_value += (v.split(":")(0) -> v.split(":")(1))
      }
    } else {
      current_value += (value.split(":")(0) -> value.split(":")(1))
    }
    val current_price =
      current_value.map { case (k, v) => s"$k: $v" }.mkString(",")
    // propagateResult(state, current_price)
    // if (trueCounter == childs.size) {
    //   val current_price = current_value.map{ case (k,v) => s"$k: $v" }.mkString(",")
    //   propagateResult(state, current_price)
    //   trueCounter = 0
    // }
  }

  def sendUpdateValue(value: String): Unit = {
    parents.foreach(
      _.receiveUpdateValue(expression.split("[<>]").head + ":" + value)
    )
  }

  def sendNotification(): Unit = {
    if (url != "") {
      val current_price =
        current_value.map { case (k, v) => s"$k: $v" }.mkString(", ")
      //println("current_price" + current_price)
      val testRequest = Http(url)
        .postForm(
          Seq(
            "value1" -> expression,
            "value2" -> s"Current Price: $current_price",
            "value3" -> "---"
          )
        )
        .asString
      println("Notification sent <- from" + this.expression)

    }
  }

  def propagateResult(state_for_propagate: Boolean, value: String): Unit = {
    if (!parents.isEmpty) {
     
    
     // parents.foreach(_.receiveResult(state_for_propagate, value, this))
      val pattern: Regex = "Seq\\((.*?)\\)".r
      parents.foreach(x => x.expression match {
        case pattern(parent_expression) => x.receiveResult(check_state_Seq(expression, comed_Node_Buffer), value, this)
        case _: String => x.receiveResult(state_for_propagate, value, this)
      })
    }
    // if (state == true) {
    //   state = false
    // }

    def check_state_Seq(self_expr:String, target_expr:ListBuffer[Node]): Boolean = {
      
      def check_Array_Order(arr1: Array[String], arr2: Array[String]):Boolean = {
      if (arr1.length != arr2.length) {
        false
      } else {
        var i = 0
        while (i < arr1.length) {
          if (arr1(i) != arr2(i)) {
            return false
          }
          i += 1
        }
        true
      }
    }
      // println("CHECK STATE SEQ!!!!!!!!!!!!!")
      // println(comed_Node_Buffer.size)
      // println(childs.size)
      if (comed_Node_Buffer.size == childs.size) {
        //println("CHECK STATE SEQ!!!!!!!!!!!!!")
        // val pattern: Regex = "Seq\\((.*?)\\)".r
        val expr1 = self_expr.split('^')
        var expr2 = ArrayBuffer[String]()
        for(node <- target_expr) {
          expr2 += node.expression
        }
        println(expr1.mkString(","))
        println(expr2.mkString(","))
        // println(expr1 == expr2.toArray)
        
        println(check_Array_Order(expr1, expr2.toArray))
        check_Array_Order(expr1, expr2.toArray)
        
        //println(expr1 == expr2)
        // val isOrdered: Boolean = comed_Node_Buffer_With_Time.zip(comed_Node_Buffer_With_Time.tail)
        // .forall { case ((_, time1), (_, time2)) => time1 < time2 }
        // isOrdered
      } else {
        //println("here ?")
        false
      }
      
      
      
    }
    

  }

}

case class Leaf_Node(val expression: String) extends Node {
  // val expression: String = _expression
  var parents: ArrayBuffer[Node] = ArrayBuffer[Node]()
  var childs: ArrayBuffer[Node] = ArrayBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  var trueCounter = 0
  var url: String = ""

  // var value:String = ""
  def receiveResult(new_state: Boolean,value: String,sendsFrom: Node = new Leaf_Node("0")): Unit = {
    // println(expression + " receive result: " + new_state)

    (expression + " receive result: " + new_state + " with value: " + value)
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
    // propagateResult(new_state, value)
    // println(expression + " receive result: " + new_state)

  }

  def propagateResult(
      result: Boolean,
      value: String /*,sendsFrom: String = expression*/
  ): Unit = {
    parents.foreach(
      _.receiveResult(result, expression.split("[<>]").head + ":" + value, this)
    )
    // if (state == true) {
    //   state = false
    // } else {
    //   state = true
    // }
  }
  def updateValue(value: String): Unit = {
    parents.foreach(
      _.receiveUpdateValue(expression.split("[<>]").head + ":" + value)
    )
  }

  def setUrl(url: String): Unit = {}
  def receiveUpdateValue(value: String): Unit = {}
}

case class ATree(name: String) extends Serializable {
  var non_leaf_node :HashMap[Long, Node] = HashMap[Long, Node]()
  var hen: HashMap[Long, Node] = HashMap[Long, Node]()
  var root: ListBuffer[Node] = ListBuffer[Node]()
  var leafNodeArrayBuffer: ArrayBuffer[Node] = ArrayBuffer[Node]()
  var groupBySource_Map: Map[String, ArrayBuffer[Node]] =
    Map[String, ArrayBuffer[Node]]()
  var switch_Node_Map = Map[String, Switch_Node]()
  var nodes_ancestors_expression_Map: HashMap[String, Set[String]] =
    HashMap[String, Set[String]]()
  def add_query(query: String): Unit = {
    if ( query.contains('∨') ) {
      query.split('∨').foreach(x => insert(x,true))
    } else {
      insert(query, true)
    }
    
  }


  def insert(_expression: String, is_userQuery: Boolean): Node = {

    val id = generateID(_expression )
    //println(_expression + " ID:  " + id )
    if (hen.getOrElse(id,0) != 0) {
      //println("Already in the tree !!!!!!!!!!!!!!!!!")
      hen(id).useCount += 1
      hen(id)
    } else {
      for (predicate <- _expression.split('^')) {
        
      if (is_userQuery) {
        if(nodes_ancestors_expression_Map.contains(predicate)) {
          // Key exists, retrieve the ListBuffer and add the new element
          nodes_ancestors_expression_Map(predicate) += _expression
        } else {
          // Key doesn't exist, create a new ListBuffer and associate it with the key
          val newSet = Set[String](_expression)
          nodes_ancestors_expression_Map.put(predicate, newSet)
        }
        } 
      }
      //println("come to here")
      var childExprs = List(_expression)
      if (_expression.split('^').length > 2 && hen.size> 1){
        childExprs = reorganize(_expression)
      }
      
      var flag = true
      if (childExprs.isEmpty) {
        childExprs = List(_expression)
        flag = false
      }
      //println(_expression +" expression's childExprs: " + childExprs.mkString(","))
      //println("flag: " + flag)
      var childNodes: ListBuffer[Node] = ListBuffer[Node]()

      if (_expression.length() > 1) {
        if (childExprs.size > 1) {
          for (expr <- childExprs) {
            //println("!!!!!!!!!!!!!!!!!!!expr: " + expr)
            var childNode = insert(expr,false)
            childNodes += childNode
          }
        } else {
          if (childExprs(0).contains('^')) {
            val expr = childExprs(0)
            var predicates = expr.split('^')
            for (s <- predicates) {
              if (!s.contains("Seq")) {
                val childNode = insert(s,false)
                childNodes += childNode
              } else if (s.contains("Seq")) {
                val expr = s.substring(4, s.length() - 1)
                var predicates = expr.split(",")
                val exprwithand = predicates.mkString("^")
                val childNode = insert(exprwithand,false)
                childNodes += childNode
              }
            }
          } else if (childExprs(0).contains("Seq")) {
            val expr = childExprs(0).substring(4, childExprs(0).length() - 1)
            var predicates = expr.split(",")
            val exprwithand = predicates.mkString("^")
            val childNode = insert(exprwithand,false)
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
      if (node.expression.split('^').length >= 2 || node.expression.contains("Seq")) {non_leaf_node += (id -> node) }
      hen += (id -> node)
      node
    }

  }

  private def createNewNode(expr: String, childNodes: List[Node]): Node = {
    val id = generateID(expr)
    val node = if (!expr.contains('^') && !expr.contains("Seq")) {
      new Leaf_Node(expr)
    } else {
      new Inner_Node(expr, 0)
    }
    for (childNode <- childNodes) {
      node.childs += childNode
      childNode.parents += node
    }

    hen += (id -> node)
    node
  }

  private def generateID(_expression: String): Long = {
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

  private def reorganize(_expression: String): List[String] = {

    //println("Reorganize: " + _expression)
    var u = ListBuffer(List(_expression))
      .map(_.flatMap(_.split("[\\^∨]")).toSet)
      .toSet
    //println(u)

    var c: ListBuffer[String] = ListBuffer[String]()
    var round = 0
    //println("U :" + u.mkString(", "))
    breakable {
      while (u.nonEmpty) {

        val s = selectAn_S_that_maximizes_insect_in_Hen(u, _expression)
        //println("s: " + s)
        if (s.isEmpty) {
          //println("trigger break")
          break()
        }
        u = u.map(_.filter(!s.contains(_)))
        //println("u: " + u)
        c += setRecoveryToStringWithAnd(s)
        //println("c: " + c)
        round += 1
      }

    }
    if (round > 1) {}
    val foruni = u.map(x => x.toList).toList

    c.appendAll(foruni.flatten).toList

  }

  // private def find_max_intersect(
  //     set1: Set[String],
  //     target: HashMap[Int, Node]
  // ): Set[String] = {
  //   var maxinterSet: Set[String] = Set.empty
  //   for ((id,node) <- for_find_max_intersect_List) {
  //     val interSet = set1.intersect(exprToPredicateSet(node.expression))
  //     if (interSet.size > maxinterSet.size) {
  //       maxinterSet = interSet
  //     }
  //   }

  //   maxinterSet

  // }

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
    //println("expr :  " + expr)

    for (predicate <- expr.split('^')) {
      if (predicate.contains('∨')) {
        predicate.split('∨').foreach(stringSet += _)
      } else {
        stringSet += predicate
      }
    }
    //println("-------------stringSet :  "  + stringSet)
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

    def collectAncestors (node: Node): Set[Node] = {
      var ancestors: Set[Node] = Set.empty
      if (node.parents.nonEmpty) {
        for (parent <- node.parents) {
          // if(parent.expression.contains("Seq")) 
          ancestors += parent
          ancestors ++= collectAncestors(parent)
        }
      }
      ancestors
    }



    //println(_expression +"!!!! expression")
    var maxinterSet: Set[String] = Set.empty
    //val no_self_for_find_max = for_find_max_intersect_List - generateID(_expression)
    //val no_self_for_find_max = (for_find_max_intersect_List - generateID(_expression)).values.map(_.expression).toSeq
    var no_self_for_find_max:List[String] = List.empty
    def findDuplicates[T](list: List[T]): List[T] = {
      val occurrences = mutable.HashMap.empty[T, Int]
      //val duplicates = mutable.ListBuffer.empty[T]
      var max_element_count = 0
      var max_element = List.empty[T]
      for (element <- list) {
        val count = occurrences.getOrElse(element, 0) + 1
        occurrences(element) = count
        //here should to find max count 
        if (count > max_element_count) {
          max_element_count = count
          max_element = List(element)
        }
        if (count == 2) {
          //duplicates += element
        }
      }

      //duplicates.toList // old version
     //println("max element !!!! "+max_element)
      max_element //new  version
    }

    def find_max_appears_expression(predicate_set: Set[String]): List[String] = {
      
      val occurrences = mutable.HashMap.empty[String, Int] 
      var max_element_count = 0
      var max_element = List.empty[String]
      var find_max_flag = false
      breakable {
      for(predicate <- predicate_set) {
          breakable {
            for(ancestors  <-  nodes_ancestors_expression_Map(predicate)) {
            // println("ancestor" + ancestors)
                if (ancestors == _expression) {

                } else {
                val count = occurrences.getOrElse(ancestors, 0) + 1
                occurrences(ancestors) = count
                  if (count > max_element_count) {
                  max_element_count = count
                  max_element = List(ancestors)
                  }
                  if (count > predicate_set.size/2) {
                    find_max_flag = true
                    break();
                  }
                }
                
            } 
          }
          if (find_max_flag) {
            break()
          }
        }
      }

      //println("max element : " + max_element.mkString(","))



      max_element
    }



    def check_predicate_in_tree_or_not(predicate_set : Set[String]): Boolean = {
      var temp_List: List[String] = List.empty
      var flag = false
      var counter = 0
  //     // par way 
  //     val parallelPredicateSet = predicate_set.par
  //     parallelPredicateSet.foreach { predicate =>
  //   hen.getOrElse(generateID(predicate), 0) match {
  //     case 0 => counter += 0
  //     case _ =>
  //       counter += 1
  //       val ancestors_Expression_Set = collectAncestors(hen(generateID(predicate))).map(_.expression)
  //       temp_List ++= ancestors_Expression_Set.toList
  //   }
  // }
      // 3:01
      for (predicate <- predicate_set) {
        //println(predicate + " predicate")
        hen.getOrElse(generateID(predicate),0) match {
          case 0 => counter += 0
          case _ => {counter += 1 ;
              // val ancestors_Expression_Set = collectAncestors(hen(generateID(predicate))).map(_.expression) ; 
              // //no_self_for_find_max ++= ancestors_Expression_Set
            
              // temp_List ++= ancestors_Expression_Set.toList
            }
        }
      }
      //no_self_for_find_max = findDuplicates(temp_List).sortBy(_.split('^').length).reverse 
      no_self_for_find_max = find_max_appears_expression(predicate_set)
      //println("!!!!!!!!!!!!!!!!!!" +no_self_for_find_max)
      //println("notice!!!!!!!!!!! "+no_self_for_find_max)
      if (counter > 1) {
        flag = true
      }
      flag
    }
    //val no_self_for_find_max = hen - generateID(_expression)
    
    for (target <- target_set) {
      if(check_predicate_in_tree_or_not(target)) {
        for (element <- no_self_for_find_max) {
          var interSet: Set[String] = Set.empty
          //println(target.toString() + " with " + exprToPredicateSet(element).toString() + "----" + interSet.toString() + " == interSet")
          interSet = target.intersect(exprToPredicateSet(element))
          //println(target.toString + " with " + exprToPredicateSet(node.expression).toString + "---" +  interSet.toString + "== intersect set")
          if (interSet.size > maxinterSet.size && interSet.size != target.size && interSet.size >1) {
            //println( "here!!!!!!!!!! is interSet size equals target.size ?" + (interSet.size == target.size).toString())
          
            maxinterSet = interSet
            //println(maxinterSet)
          } else if(interSet.size == target.size) {
            val new_interSet = interSet.dropRight(1)
            maxinterSet = new_interSet
          }
        }
        //println( target.toString +  "'s "+ "Max intersect set: " + maxinterSet.toString())
      }
    }
   /*import scala.collection.parallel.ParIterable  
   for (target <- target_set) {
    if (check_predicate_in_tree_or_not(target)) {
      //var interSet: Set[String] = Set.empty
      
      val interSets: ParIterable[Set[String]] = no_self_for_find_max.par.flatMap { expression => 
        //val interSet = target.intersect(exprToPredicateSet(expression))
        var interSet: Set[String] = Set.empty
       if(interSet.size <= exprToPredicateSet(expression).size) {
          interSet = target.intersect(exprToPredicateSet(expression)) 
        } else {
          no_self_for_find_max = List.empty
        }
        if (interSet.size != target.size && interSet.size > 1){
          //println("Some here !!" + interSet.size + " : " + target.size )
          Some(interSet)
        } else if ( interSet.size == target.size) {
          val new_interSet = interSet.drop(interSet.size - 1)
          Some(new_interSet)
        }
        else {
          None
        }
        
      
      }
    

      val localMaxInterSet = interSets.reduceOption { (a, b) =>
        if (a.size > b.size) a
        else b
      }

      localMaxInterSet.foreach { localMax =>
        synchronized {
          if (localMax.size > maxinterSet.size)
            maxinterSet = localMax
        }
      }
    }
  }*/



    //println("Max intersect set: " + maxinterSet)
    no_self_for_find_max = List.empty
    maxinterSet
  }

  def selfAdjust(newNode: Node): Unit = {
    //println(newNode.expression + " --------expr!!!!!!!!!!!!!")
    var childNodes = newNode.childs
    //println(childNodes.mkString(", "))
    for (i <- 0 until childNodes.size) {
      if (i < childNodes.size) {
      //println("child node size : " + childNodes.size)
      // println(i)
      var j = 0
      
      while (j < childNodes(i).parents.size - 1  && childNodes(i).parents.nonEmpty) {
        // println(i,j + "childNodes parent size: " + childNodes(i).parents.size)  
        //println(childNodes(i).parents.mkString(", "))
        if (
          parentExpressionContainsNewExpression(
            childNodes(i).parents(j).expression,
            newNode.expression
          )
        ) {

          //childNodes(i).parents(j).childs -= childNodes(i)
          if (childNodes(i).parents.nonEmpty && j < childNodes(i).parents.size && childNodes(i).parents(j) != newNode) {childNodes(i).parents(j).childs -= childNodes(i)}
          //println("i,j  here ---------------" + i + ", " + j + "child nodes size = " + childNodes.size)
          
            if (!childNodes(i).parents(j).childs.contains(newNode)) {
              childNodes(i).parents(j).childs += newNode
            }
          
         
          //childNodes(i).parents -= childNodes(i).parents(j)
          //childNodes(i).parents -= childNodes(i).parents(j)
          
            if ( !newNode.parents.contains(childNodes(i).parents(j)) && childNodes(i).parents(j) != newNode) {
              newNode.parents += childNodes(i).parents(j)
            }
          
          
            childNodes(i).parents -= childNodes(i).parents(j)
          

        } else {
          j += 1
        }
        
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

  def from_hen_collect_leaf_Node_to_ArrayBuffer(
      target: HashMap[Long, Node],
      con: ArrayBuffer[Node]
  ): Unit = {
    target.foreach(x =>
      // if (!x._2.expression.contains('^') && !x._2.expression.contains("Seq")) leafNodeArrayBuffer += x._2
      if (x._2.isInstanceOf[Leaf_Node]) con += x._2
    )
  }

  def create_Switch_Node_from_groupbySource_Map(
      source: String,
      target: Array[Node]
  ): Unit = {
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
    switch_Node_final.create_equal_symbol_Map(target2.toArray)
    switch_Node_Map += (source -> switch_Node_final)
  }

}


