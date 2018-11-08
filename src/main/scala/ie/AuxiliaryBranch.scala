package ie

import edu.stanford.nlp.ling.IndexedWord
import util.CoreNLPUtil

/**
  * A class to represent an branch of a word. A branch
  * provides complementary information about the word
  * such as its negation, or nominal dependent.
  * @param branchType The type of the branch with respect
  *                   to the original word.
  * @param words The words which make up the branch.
  */
case class AuxiliaryBranch(branchType: String, words: List[IndexedWord]) {
  override def toString: String = branchType + ": " + CoreNLPUtil.indexWordsToString(words)
}