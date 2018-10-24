package ie

import edu.stanford.nlp.ling.IndexedWord
import util.CoreNLPUtil

case class Predicate(representative: IndexedWord, compoundParts: List[IndexedWord], branches: List[AuxiliaryBranch]) {
  override def toString: String = {
    val compoundString = if (compoundParts.nonEmpty) "[" + CoreNLPUtil.indexWordsToString(compoundParts) + "]" else ""
    val branchesString = if (branches.nonEmpty) "[" + branches.map(b => b.toString) + "]" else ""

    representative.word() + compoundString + branchesString
  }
}
