import scalaj.http._

object TestNodes {
  def main(args: Array[String]): Unit = {

    // val array1 = Array(
    //   Leaf_Node("ETH>9"),
    //   Leaf_Node("ETH>11"),
    //   Leaf_Node("ETH>8"),
    //   Leaf_Node("ETH<3")
    // )

    // val switch1 = new Switch_Node("Switch1", array1)

// val a = switch1.sortedWithExpression(array1).zipWithIndex
//     a.foreach(x => println(x))

  //   val b = switch1.createTrue_False_Map(array1)
  //   b.foreachEntry{case (k, v) => println("Key:" + k)
  //   for(i <- v.indices) {
  //     if(i==0) {
  //       println("True Nodes")
  //       for(j <- v(i).indices) {
  //       println(v(i)(j))}

  //       } 
  //     else {
  //       println("False Nodes")
  //       for(j <- v(i).indices) {
  //       println(v(i)(j))
  //       }
  //     }
  //   }
  // }
  //   //println(b.size)
  //   println("size::::::::" +switch1.true_false_Map.size)

  // val c = switch1.receiveValueThenForward(1)
  // println("c-----------------------")
  // for (i <- c.indices) {
  //   if(i==0) {
  //       println("True Nodes")
  //       for(j <- c(i).indices) {
  //       println(c(i)(j))}

  //       } 
  //     else {
  //       println("False Nodes")
  //       for(j <- c(i).indices) {
  //       println(c(i)(j))
  //       }
  //     }
  // }
  // val incomingValue = 3
  // println(s"-----incoming value = $incomingValue ------------------")
  // val d = switch1.receiveValueThenForward(incomingValue)

  //println("d-----------------------")
  // for (i <- d.indices) {
  //   if(i==0) {
  //       println("True Nodes")
  //       for(j <- d(i).indices) {
  //       println(d(i)(j))}

  //       } 
  //     else {
  //       println("False Nodes")
  //       for(j <- d(i).indices) {
  //       println(d(i)(j))
  //       }
  //     }
  // }

//   def findIndex(arr: Array[Int], value: Int): Int = {
//   var left = 0
//   var right = arr.length - 1
//   var result = -1
//   if (arr.contains(value)) {
//     result = arr.indexOf(value) //and 如果EXPR 為> idx -1  如果EXPR 為< idx +1
//   }
//   while (left <= right) {
//     val mid = left + (right - left) / 2
//     if (arr(mid) <= value) {
//       result = mid
//       left = mid + 1
//     } else {
//       right = mid - 1
//     }
//   }
//   result
// }
//   val array2 =Array(3, 8, 9, 11)
//   val d = findIndex(array2, 9)
//   println(d)


  //   def generateID(_expression: String): Long = {
  //   val predicatesAndOperators: List[Char] = _expression.toList
  //   var id: Int = 0
  //   for (char <- predicatesAndOperators) {
  //     id += char.hashCode() * char.hashCode()
  //   }

  //   id
  // }

  // val tree = new ATree("My ATree")
  // tree.insert("BTC>3^ETH>9^DOGE>10")
  // tree.insert("BTC>3^ETH>9^SOL>20")
  // tree.hen.foreach(x => tree.checkNodeChildsParent(x._2))
  // tree.from_hen_collect_leaf_Node_to_ArrayBuffer(tree.hen, tree.leafNodeArrayBuffer)
  // println("-----------------------")
  // tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).setUrl("https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM")
  // println(tree.hen(generateID("BTC>3^ETH>9^DOGE>10")).url)
  // tree.groupBySource_Map =  tree.leafNodeArrayBuffer.groupBy(x => x.expression.takeWhile(_ != '>').takeWhile(_ != '<'))
  // tree.groupBySource_Map.map(x => tree.create_Switch_Node_from_groupbySource_Map(x._1, x._2.toArray))
  // //val groupMap2 = groupMap.map(x => (x._1, x._2.map(_.expression)))
  // //println(groupMap2)
  // println("-----------------------")

  // val incomingValue = "BTC:3"
  // //tree
  // tree.switch_Node_Map(incomingValue.split(":").head).receiveValueThenForward(incomingValue.split(":").last.toDouble)

  
    //Http("https://maker.ifttt.com/trigger/scala_event/with/key/cyMr3y7V3Np-gzMAhWE8HM").postForm(Seq("value1" -> "dfhgdfhdhdgfhgf", "value2" -> "---" ,"value3" -> "---")).asString


    //Http("https://maker.ifttt.com/trigger/scala_event/json/with/key/cyMr3y7V3Np-gzMAhWE8HM").postData("""{"value1":"expression","value2":"---","value3":"---"}""")
  
  }
}
