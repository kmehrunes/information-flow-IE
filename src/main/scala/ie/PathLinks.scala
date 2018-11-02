package ie

import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.semgraph.SemanticGraph

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

  def getLinkingPaths(paths: Iterator[InformationPath]): Map[Int, List[InformationPath]] = {
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

  /**
    * Links a path to its extended path using links from the words in the
    * predicate. This function adds object paths to the path.
    *
    * @param rootPath    The original path which we need to find the links to
    * @param wordPathMap An Integer-Path map to find paths based on their
    *                    words
    * @param graph       The dependency graph to operate on
    */
  private def linkPathsByPredicate(graph: SemanticGraph, rootPath: InformationPath,
                                   wordPathMap: Map[Integer, List[InformationPath]]): List[(InformationPath, InformationPath)] =
  {
    val predicateRepresentative: IndexedWord = rootPath.predicate.representative
    val predicateLinks: List[IndexedWord] = DependencyGraphs.findConnectingRelations(graph, predicateRepresentative)

    val buffer = new ListBuffer[(InformationPath, InformationPath)]

    for (linkingWord <- predicateLinks) {
      val linkedPaths = wordPathMap.getOrElse(linkingWord.index, List.empty)

      for (path <- linkedPaths) {
        if (path != rootPath) {
          buffer += Tuple2(rootPath, path)
        }
      }
    }

    buffer.toList
  }

  /**
    * Links a path to its extended path using links from the words in the
    * object, if any exist. This function adds auxiliary paths to the path.
    *
    * @param rootPath    The original path which we need to find the links to
    * @param wordPathMap An Integer-Path map to find paths based on their
    *                    words
    * @param graph       The dependency graph to operate on
    */
  private def linkPathsByObject(graph: SemanticGraph, rootPath: InformationPath,
                                wordPathMap: Map[Integer, List[InformationPath]]): List[(InformationPath, InformationPath)] =
  {
    val objectLinks = rootPath.obj.flatMap(word => DependencyGraphs.findConnectingRelations(graph, word))
    val buffer = new ListBuffer[(InformationPath, InformationPath)]

    for (linkingWord <- objectLinks) {
      val linkedPaths = wordPathMap.getOrElse(linkingWord.index, List.empty)

      for (path <- linkedPaths) {
        if (path != rootPath) {
          buffer += Tuple2(rootPath, path)
        }
      }
    }

    buffer.toList
  }
}
