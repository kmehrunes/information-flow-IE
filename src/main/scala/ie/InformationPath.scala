package ie

import edu.stanford.nlp.ling.IndexedWord
import util.CoreNLPUtil

case class InformationPath(var subj: List[IndexedWord], var predicate: Predicate, var obj: List[IndexedWord],
                           var indirectObj: List[IndexedWord] = List.empty,
                           var predicateLinks: List[InformationPath] = List.empty,
                           var objectLinks: List[InformationPath] = List.empty)
{

  override def toString: String = toStringInd(this)

  private def toStringInd(path: InformationPath, ind: Int = 0, prefix: String = ""): String = {
    val builder = new StringBuilder

    (0 until ind).foreach(_ => builder.append('\t'))
    builder.append(prefix)

    builder.append("(subj: ")
    builder.append(CoreNLPUtil.indexWordsToString(path.subj))
    builder.append(", pred: ")
    builder.append(path.predicate.toString)

    if (path.obj.nonEmpty) {
      builder.append(", obj: ")
      builder.append(CoreNLPUtil.indexWordsToString(path.obj))
    }
    builder.append(")")

    if (path.predicateLinks.nonEmpty) {
      path.predicateLinks.foreach(p => {
        builder.append('\n')
        builder.append(toStringInd(p, ind + 1, "predicate link: "))
      })
    }

    if (path.objectLinks.nonEmpty) {
      path.objectLinks.foreach(p => {
        builder.append('\n')
        builder.append(toStringInd(p, ind + 1, "object link: "))
      })
    }

    builder.toString
  }
}
