import scala.util.Random
import com.google.gson.annotations.Until

object GenerateQuery {
 def generateQueryExpression(): String = {
  val predicates = Random.between(3, 16)
  val operators = Array("<", ">", "=")
  val baseQueries = List("AAPL", "MSFT" , "AMZN", "GOOGLE", "FB","INTC", "CSCO", "NFLX", "ADBE", "TSLA", "NVDA", "PYPL", "CMCSA",
    "SBUX", "HD", "QCOM", "ZM", "INTC", "AMD", "PEP", "MRNA", "AMAT", "CHTR", "EA", "GNC", "COST", "ADBE", "MDLZ", "JD", "EBAY", "TOYOTA", "MAR", "ATVI",
    "REGN", "ILMN", "NISSAN"
    )
  val expression = (1 to predicates).map { _ =>
    
    val leftOperand = Random.alphanumeric.take(Random.between(3,5)).mkString.toUpperCase()
    val rightOperand = Random.between(10, 100).toString
    val operator = operators(Random.nextInt(operators.length))

    s"$leftOperand$operator$rightOperand"
  }.mkString("^")

  expression
  }
  def main(args: Array[String]): Unit = {
    val queryExpression = generateQueryExpression()
    println(queryExpression)
  }
}
