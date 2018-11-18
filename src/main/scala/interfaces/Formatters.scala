package interfaces

import ie.{AuxiliaryBranch, ExtractionResult, InformationPath, Predicate}

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import util.CoreNLPUtil

object Formatters {

  private def pathJson(path: InformationPath): JsonAST.JObject = {
    var json = new JsonAST.JObject(List.empty)

    if (path.subj.nonEmpty)
      json ~= ("subject" -> CoreNLPUtil.indexWordsToString(path.subj))

    json ~=  ("predicate" -> path.predicate.toString)

    if (path.obj.nonEmpty)
      json ~= ("object" -> CoreNLPUtil.indexWordsToString(path.obj))

    if (path.indirectObj.nonEmpty)
      json ~= ("indirectObject" -> CoreNLPUtil.indexWordsToString(path.indirectObj))

    if (path.predicateLinks.nonEmpty)
      json ~= ("predicateLinks" -> path.predicateLinks.map(pathJson))

    if (path.objectLinks.nonEmpty)
      json ~= ("objectLinks" -> path.objectLinks.map(pathJson))

    json
  }

  private def resultJson(result: ExtractionResult) = {
    ("sentence" -> result.sentence) ~ ("paths" -> result.paths.map(pathJson))
  }

  def formatJson(results: List[ExtractionResult], prettyPrint: Boolean): String = {
    val json = "results" -> results.map(resultJson)

    if (prettyPrint) pretty(render(json)) else compact(render(json))
  }

  def formatPlain(results: List[ExtractionResult]): String = {
    val builder = new StringBuilder

    results.foreach(res => {
      builder.append("> ")
        .append(res.sentence)
        .append("\n")

      res.paths.foreach(path => {
        builder.append(path)
          .append("\n")
      })
    })

    builder.toString
  }
}
