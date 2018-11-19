package interfaces

import java.io.PrintWriter

import ie.InformationExtraction
import org.rogach.scallop._

import scala.io.Source

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val input: ScallopOption[String] = opt[String]()
  val format: ScallopOption[String] = opt[String](default = Some("plain"),
    validate = List("plain", "json").contains)
  val file: ScallopOption[String] = opt[String]()
  val output: ScallopOption[String] = opt[String](default = Some("stdout"))

  requireOne(file, input)

  verify()
}

object CLI {

  def writeToOutput(result: String, output: String): Unit = {
    if (output.equals("stdout")) {
      println(result)
    }
    else {
      val writer = new PrintWriter(output)
      writer.write(result)
      writer.close()
    }
  }

  def fromFile(path: String, format: String, output: String): Unit = {
    val lines = Source.fromFile(path).getLines().filter(line => !line.trim.isEmpty)
    val results = lines.flatMap(paragraph => InformationExtraction.runPipeline(paragraph)).toList

    val formattedResults = if (format.equals("plain"))
      Formatters.formatPlain(results)
    else
      Formatters.formatJson(results, prettyPrint = true)

    writeToOutput(formattedResults, output)
  }

  def fromInput(input: String, format: String, output: String): Unit = {
    val results = ie.InformationExtraction.runPipeline(input)

    val formattedResults = if (format.equals("plain"))
      Formatters.formatPlain(results)
    else
      Formatters.formatJson(results, prettyPrint = true)

    writeToOutput(formattedResults, output)
  }

  def main(args: Array[String]) {
    val conf = new Conf(args)

    if (conf.input.isDefined) {
      fromInput(conf.input.getOrElse(""), conf.format.getOrElse(""), conf.output.getOrElse(""))
    }
    else if (conf.file.isDefined) {
      fromFile(conf.file.getOrElse(""), conf.format.getOrElse(""), conf.output.getOrElse(""))
    }
  }
}
