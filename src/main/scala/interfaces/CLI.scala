package interfaces

import ie.{ExtractionResult, InformationExtraction}
import org.rogach.scallop._

import scala.io.Source

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val input: ScallopOption[String] = opt[String]()
  val format: ScallopOption[String] = opt[String](default = Some("plain"),
    validate = List("plain", "json").contains)
  val file: ScallopOption[String] = opt[String]()

  requireOne(file, input)
  verify()
}

object CLI {

  def printExtractionResult(result: ExtractionResult): Unit = {
    println("Sentence: " + result.sentence)
    result.paths.foreach(path => {
      println(path)
      println()
    })
  }

  def fromFile(path: String, format: String): Unit = {
    val lines = Source.fromFile(path).getLines().filter(line => !line.trim.isEmpty)
    val results = lines.flatMap(paragraph => InformationExtraction.runPipeline(paragraph)).toList

    if (format.equals("plain")) {
      println(Formatters.formatPlain(results))
    }
    else {
      println(Formatters.formatJson(results, prettyPrint = true))
    }
  }

  def fromInput(input: String, format: String): Unit = {
    val results = ie.InformationExtraction.runPipeline(input)

    if (format.equals("plain")) {
      println(Formatters.formatPlain(results))
    }
    else {
      println(Formatters.formatJson(results, prettyPrint = true))
    }
  }

  def main(args: Array[String]) {
    val conf = new Conf(args)

    if (conf.input.isDefined) {
      fromInput(conf.input.getOrElse(""), conf.format.getOrElse(""))
    }
    else if (conf.file.isDefined) {
      fromFile(conf.file.getOrElse(""), conf.format.getOrElse(""))
    }
  }
}
