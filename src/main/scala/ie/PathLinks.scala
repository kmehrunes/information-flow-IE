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

  private def linkWordsToPaths(paths: List[InformationPath]): Map[Int, List[InformationPath]] = {
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
  private def findPredicateLinks(graph: SemanticGraph, rootPath: InformationPath,
                                   wordPathMap: Map[Int, List[InformationPath]]): List[InformationPath] =
  {
    val predicateRepresentative: IndexedWord = rootPath.predicate.representative
    val predicateLinks: List[IndexedWord] = DependencyGraphs.findConnectingRelations(graph, predicateRepresentative)

    val buffer = new ListBuffer[InformationPath]

    for (linkingWord <- predicateLinks) {
      val linkedPaths = wordPathMap.getOrElse(linkingWord.index, List.empty)

      for (path <- linkedPaths) {
        if (path != rootPath) {
          buffer += path
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
  private def findObjectLinks(graph: SemanticGraph, rootPath: InformationPath,
                                wordPathMap: Map[Int, List[InformationPath]]): List[InformationPath] =
  {
    val objectLinks = rootPath.obj.flatMap(word => DependencyGraphs.findConnectingRelations(graph, word))
    val buffer = new ListBuffer[InformationPath]

    for (linkingWord <- objectLinks) {
      val linkedPaths = wordPathMap.getOrElse(linkingWord.index, List.empty)

      for (path <- linkedPaths) {
        if (path != rootPath) {
          buffer += path
        }
      }
    }

    buffer.toList
  }

  private def linkPaths(graph: SemanticGraph, rootPath: InformationPath,
                        wordPathMap: Map[Int, List[InformationPath]]): InformationPath =
  {
    val predicateLinks = findPredicateLinks(graph, rootPath, wordPathMap)
    val objectLinks = findObjectLinks(graph, rootPath, wordPathMap)

    rootPath.copy(predicateLinks = predicateLinks, objectLinks = objectLinks)
  }

  def linkPaths(graph: SemanticGraph, paths: List[InformationPath]): List[InformationPath] = {
    val pathsMap = linkWordsToPaths(paths)

    paths.map(path => linkPaths(graph, path, pathsMap))
  }
}
