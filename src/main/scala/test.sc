import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

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
  def receiveResult(result: Boolean): Unit 
  def setUrl(url:String):Unit 
  //def this() = this("",0L)
}

case class NodeSwitch(_name: String, val targetNodes: Array[Node]) extends Serializable {
  var true_false_Map: Map[Int, Array[Array[Node]]] = Map(0 -> Array(Array()), 1 -> Array(Array()))
  def initTrue_False_Map(targetNodes: Array[Node]): Array[Node] = {
    val sortedWithExpression = targetNodes.sortBy{ node => val operandIdx = node.expression.indexWhere(Set('>','<','=').contains)
    node.expression.substring(operandIdx+1).toInt  
    }
    sortedWithExpression
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

case class Leaf_Node(val expression: String) extends Node {
  //val expression: String = _expression
  var parents: ListBuffer[Node] = ListBuffer[Node]()
  var childs: ListBuffer[Node] = ListBuffer[Node]()
  var useCount: Int = 0
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  var trueCounter = 0
  var url:String = ""
  def receiveResult(new_state: Boolean): Unit = {
    if (this.isInstanceOf[Leaf_Node]) {
      if(state != new_state) {
        state = new_state
        propagateResult(state)
      }
      //println(expression + "receive result: " + result)
    }
  }

  def propagateResult(result: Boolean): Unit = {
    parents.foreach(_.receiveResult(result))
  }

  def setUrl(url: String): Unit = {}

}


val array1 = Array(Leaf_Node("ETH>9"), Leaf_Node("ETH>11"), Leaf_Node("ETH>8"))

val switch1 = new Switch_Node("switch1", array1)

val a = switch1.initTrue_False_Map(array1)
println(a)