import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.P
import scalaj.http._
import java.io.Serializable
import algebra.lattice.Bool



class TestNode(_name: String) {
  val name = _name
  var childs: ListBuffer[TestNode] = ListBuffer[TestNode]()
  var parents: ListBuffer[TestNode] = ListBuffer[TestNode]()
  var childExprs: ListBuffer[List[String]] = ListBuffer[List[String]]()
  var state: Boolean = false
  var comes_or_not_Array: ArrayBuffer[TestNode] = ArrayBuffer[TestNode]()
  var url:String = ""
  def setChilds(child: TestNode): Unit = {
    childs += child
    childExprs += List(child.name)
  }
  def setParents(parent: TestNode): Unit = {
    parents += parent
  }
  def send_result(value: Boolean): Unit = {
    parents.foreach(parent => {
      parent.receive_result(value, this)
    })
  }
  def receive_result(comes_state: Boolean, comes_from: TestNode): Unit = {
    if (comes_state == true && comes_or_not_Array.contains(comes_from) == false) {
      comes_or_not_Array += comes_from
      state = check_state()
      
    } else if (comes_state == false && comes_or_not_Array.contains(comes_from) == true) {
      comes_or_not_Array -= comes_from
      state = check_state()
    }
    
    if (!parents.isEmpty ) {
      send_result(state)
    }
    if (url != "") {
      if (state) {
        println("True trigger true action send http request to"+ url) 
      } else {
        println("False trigger false action")
      }
    }
    def check_state(): Boolean  = {
      if (comes_or_not_Array.length == childs.length) {
        return true
      } else {
        return false
      }
    }
  }
  def setUrl(url:String):Unit = {
      this.url = url
    }

}



object NodeTest {
  def main(args: Array[String]): Unit = {

  val abc = new TestNode("abc")
  abc.setUrl("http://www.baidu.com")
  val ab = new TestNode("ab")
  val a = new TestNode("a")
  val b = new TestNode("b")
  val c = new TestNode("c")
  abc.setChilds(ab)
  abc.setChilds(c)
  ab.setChilds(a)
  ab.setChilds(b)
  a.setParents(ab)
  b.setParents(ab)
  c.setParents(abc)
  ab.setParents(abc)
  a.send_result(true)
  //println(abc.comes_or_not_Array(0).name)
  println(abc.state)
  println(ab.comes_or_not_Array(0).name)
  println(ab.state)
  b.send_result(true)
  println(ab.comes_or_not_Array(0).name)
  println(ab.comes_or_not_Array(1).name)
  println(ab.state)
  println(abc.comes_or_not_Array(0).name)
  println(abc.state)
  c.send_result(true)
  println(abc.comes_or_not_Array(0).name)
  println(abc.comes_or_not_Array(1).name)
  println(abc.state)
  println("----------------------")
  a.send_result(false)
  println(abc.comes_or_not_Array(0).name)
  //println(abc.comes_or_not_Array(1).name)
  println(abc.state)
  a.send_result(false)
  println(abc.comes_or_not_Array(0).name)
  //println(abc.comes_or_not_Array(1).name)
  println(abc.state)
  println("----end-----")
  // abc.setChilds(a)
  // abc.setChilds(b)
  // abc.setChilds(c)

  // a.setParents(abc)
  // b.setParents(abc)
  // c.setParents(abc)

  // a.send_result(true)
  // println(abc.comes_or_not_Array(0).name)
  // println(abc.state)
  // b.send_result(true)
  // println(abc.comes_or_not_Array(0).name)
  // println(abc.comes_or_not_Array(1).name)
  // println(abc.state)
  
  // c.send_result(true)
  // println(abc.comes_or_not_Array(0).name)
  // println(abc.comes_or_not_Array(1).name)
  // println(abc.comes_or_not_Array(2).name)
  // println(abc.state)
  

  // b.send_result(false)
  // println(abc.comes_or_not_Array(0).name)
  // println(abc.comes_or_not_Array(1).name)
  // println(abc.state)
  // a.send_result(true)
  // println(abc.comes_or_not_Array(0).name)
  // println(abc.comes_or_not_Array(1).name)
  
  // println(abc.state)
  

  


}

}
