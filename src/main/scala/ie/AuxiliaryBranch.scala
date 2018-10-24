package ie

import edu.stanford.nlp.ling.IndexedWord
import util.CoreNLPUtil

case class AuxiliaryBranch(branchType: String, words: List[IndexedWord]) {
  override def toString: String = branchType + ": " + CoreNLPUtil.indexWordsToString(words)
}