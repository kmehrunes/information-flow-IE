package interfaces

import org.rogach.scallop._

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val input: ScallopOption[String] = opt[String]()
  val format: ScallopOption[String] = opt[String](default = Some("plain"),
    validate = List("plain", "json").contains)
  val file: ScallopOption[String] = opt[String]()
  val mode: ScallopOption[String] = opt[String](default = Some("sentences"),
    validate = List("sentences", "paragraphs", "block").contains)

  requireOne(file, input)
  dependsOnAll(mode, List(file))
  verify()
}

object CLI {

  def fromFile(path: String, mode: String, format: String): Unit = {
    println("Feature not supported")
  }

  def fromInput(input: String, format: String): Unit = {
    val result = ie.InformationExtraction.runPipeline(input)

    if (format.equals("plain")) {
      result.foreach(path => {
        println("Information flow:")
        println(path)
        println()
      })
    }
    else {
      println("feature not yet supported")
    }
  }

  def main(args: Array[String]) {
    val conf = new Conf(args)

    if (conf.input.isDefined) {
      fromInput(conf.input.getOrElse(""), conf.format.getOrElse(""))
    }
  }
}
