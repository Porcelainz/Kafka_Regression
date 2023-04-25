import org.apache.spark.SparkConf
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, SparkSession,Dataset}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.streaming.{GroupState, GroupStateTimeout}


case class WordCount(word: String, count: Long)


object MapGroupsWithStateExample {
   def updateAcrossEvents(key: String,
                          values: Iterator[String],
                          state: GroupState[WordCount]): WordCount = {
    var currentCount = state.getOption.map(_.count).getOrElse(0L)
    values.foreach(_ => currentCount += 1L)
    val updatedCount = WordCount(key, currentCount)
    state.update(updatedCount)
    updatedCount
  }

  def main(args: Array[String]): Unit = {
    
    val spark = SparkSession.builder()
      .appName("MapGroupsWithStateExample")
      .master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
     import spark.implicits._

     val lines: Dataset[String] = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "9999")
      .load()
      .as[String]

    val words = lines.flatMap(_.split(" "))
    
    val wordCounts = words.groupByKey(w => w)
      .mapGroupsWithState[WordCount,WordCount](GroupStateTimeout.NoTimeout())(updateAcrossEvents)

    wordCounts.writeStream
      .outputMode("update")
      .format("console")
      .start()
      .awaitTermination()


  }



}
