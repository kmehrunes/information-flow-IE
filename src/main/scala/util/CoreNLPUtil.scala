package util

import edu.stanford.nlp.ling.IndexedWord

object CoreNLPUtil {
  def indexWordsToString(words: Iterable[IndexedWord]): String = words.map(word => word.word()).mkString(" ")

  def isNoun(word: IndexedWord): Boolean = word.tag.startsWith("NN")
}
