

object MainTest extends  App{
  val node1 = new StreamingNode("node1","iot-data")
  //node1.run()
  
  
  val node2 = new StreamingNode("node2", "Test")
  //node2.run()

  //node1.run()
  node2.run()
  

}
