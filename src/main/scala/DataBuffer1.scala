import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.util.parsing.json._
import scala.collection.JavaConverters._
import java.util.Properties
import java.util
import scala.io.StdIn
object DataBuffer1 {
  def main(args: Array[String]): Unit = {
    consumeFromKafka("Test")
  }
  def consumeFromKafka(topic: String): Unit = {
    // val props_forConsumer = new Properties()
    // props_forConsumer.put("bootstrap.servers", "localhost:9092")
    // props_forConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    // props_forConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    // props_forConsumer.put("auto.offset.reset", "latest")
    // props_forConsumer.put("group.id", "consumer-group")
    //val consumer :KafkaConsumer[String, String] = new KafkaConsumer[String, String](props_forConsumer)
    //consumer.subscribe(util.Arrays.asList(topic))
    val props_forProducer = new Properties()
    props_forProducer.put("bootstrap.servers", "localhost:9092")
    props_forProducer.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props_forProducer.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props_forProducer)
    //val record = new ProducerRecord[String,String](topic,"key","value")
    //producer.send(record)

    var counter = 0
    val dataBuffer = new StringBuilder()
    var times = 1
    while (true) {
      val input = StdIn.readLine("Enter temp:")
      if (!input.isEmpty()){
        
        val record = s"""{"time":"$times", "temp":"$input"}"""
        
        times += 1
        counter += 1
        if (counter < 5) {
          val recordWithSpace = record + "\n"
          dataBuffer ++= recordWithSpace
        } else {
          dataBuffer ++= record
        }
        //dataBuffer ++= recordWithSpace
        println(dataBuffer)
      }

      if (counter == 5) {
        producer.send(new ProducerRecord[String,String]("temp-topic", dataBuffer.toString()))
        dataBuffer.clear()
        counter = 0
        producer.flush()

      }
      
    }

  }
}