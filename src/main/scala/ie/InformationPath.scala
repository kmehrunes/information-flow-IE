package ie

import edu.stanford.nlp.ling.IndexedWord
import util.CoreNLPUtil

case class InformationPath(var subj: List[IndexedWord], var predicate: Predicate, var obj: List[IndexedWord],
                           var indirectObj: List[IndexedWord] = List.empty,
                           var predicateLinks: List[InformationPath] = List.empty,
                           var objectLinks: List[InformationPath] = List.empty)
{

  override def toString: String = {
    val builder = new StringBuilder

    builder.append("(SUBJECT: ")
    builder.append(CoreNLPUtil.indexWordsToString(this.subj))
    builder.append(", PREDICATE: ")
    builder.append(predicate.toString)
    builder.append(", OBJECT: ")
    builder.append(CoreNLPUtil.indexWordsToString(this.obj))

    if (predicateLinks.nonEmpty) {
      builder.append(", PREDICATE LINKS: [\n\t")
      builder.append(predicateLinks.map(p => p.toString).mkString(",\n\t "))
      builder.append("\n]")
    }

    if (objectLinks.nonEmpty) {
      builder.append(", OBJECT LINKS: [\n")
      builder.append(objectLinks.map(p => p.toString).mkString(",\n "))
      builder.append("\n]")
    }

    builder.append(")")

    builder.toString
  }
}
