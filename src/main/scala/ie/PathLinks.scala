package ie

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object PathLinks {

  private def addWordToMapping(wordIndex: Int, path: InformationPath,
                               mapping: mutable.HashMap[Int, ListBuffer[InformationPath]]): Unit =
  {
    mapping.get(wordIndex) match {
      case Some(list) => list += path
      case None => mapping += (wordIndex  -> ListBuffer(path))
    }
  }

  def getLinkingPaths(paths: List[InformationPath]): Map[Int, List[InformationPath]] = {
    val wordPathMapping = new mutable.HashMap[Int, ListBuffer[InformationPath]]()

    for (path <- paths) {
      val index = path.predicate.representative.index()

      addWordToMapping(index, path, wordPathMapping)

      if (path.obj != null) {
        path.obj.foreach(word => addWordToMapping(word.index(), path, wordPathMapping))
      }

      if (path.indirectObj != null) {
        path.indirectObj.foreach(word => addWordToMapping(word.index(), path, wordPathMapping))
      }
    }

    wordPathMapping.map(entry => entry._1 -> entry._2.toList).toMap
  }
}
