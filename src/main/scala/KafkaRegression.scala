import java.util.Properties
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._
import org.apache.commons.math3.stat.regression.SimpleRegression


object KafkaRegression extends App{

  
  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "linear-regression-app")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass())
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass())

  val builder = new StreamsBuilder()

  val inputTopic = "input-topic"
  val outputTopic = "output-topic"

  val stream: KStream[String, String] = builder.stream(inputTopic)

  // Parse incoming messages into data points for linear regression
  val points: KStream[String, (Double, Double)] = stream.mapValues(value => {
    val fields = value.split(",")
    (fields(0).toDouble, fields(1).toDouble)
  })

  // Train a linear regression model on the incoming data points
  val model: KTable[String, SimpleRegression] = points.groupByKey
    .aggregate(() => new SimpleRegression(true), (key, value, aggregate) => {
      aggregate.addData(value._1, value._2)
      aggregate
    })

  // Use the model to predict output values for each new data point
  val predictions: KStream[String, Double] = points.join(model,
    (pointValue: (Double, Double), modelValue: SimpleRegression) => {
      modelValue.predict(pointValue._1)
    })

  // Write the predicted values to the output topic
  predictions.to(outputTopic)

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  streams.start()
  while(true) {
  Thread.sleep(1000)
  }

}
