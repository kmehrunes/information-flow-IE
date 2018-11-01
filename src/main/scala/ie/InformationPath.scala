package ie

import edu.stanford.nlp.ling.IndexedWord
import util.CoreNLPUtil

case class InformationPath(var subj: List[IndexedWord], var predicate: Predicate, var obj: List[IndexedWord],
                           var indirectObj: List[IndexedWord] = List.empty,
                           var objectPaths: List[InformationPath] = List.empty,
                           var auxiliaryPaths: List[InformationPath] = List.empty)
{

  override def toString: String = {
    val builder = new StringBuilder

    builder.append("(SUBJECT: ")
    builder.append(CoreNLPUtil.indexWordsToString(this.subj))
    builder.append(", PREDICATE: ")
    builder.append(predicate.toString)
    builder.append(", OBJECT: ")
    builder.append(CoreNLPUtil.indexWordsToString(this.obj))

    builder.append(", OBJECT PATHS: [")
    builder.append(objectPaths.map(p => p.toString).mkString(", "))

    builder.append("], AUX PATHS [")
    builder.append(auxiliaryPaths.map(p => p.toString).mkString(", "))

    builder.append("])")

    builder.toString
  }
}
