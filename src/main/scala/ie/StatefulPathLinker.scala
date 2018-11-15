package ie

import edu.stanford.nlp.semgraph.SemanticGraph

import scala.collection.mutable

class StatefulPathLinker(graph: SemanticGraph, wordPathMap: Map[Int, List[InformationPath]]) {

  private val memo = new mutable.HashMap[InformationPath, InformationPath]()

  private[ie] def linkPaths(rootPath: InformationPath): InformationPath =
  {
    if (memo.contains(rootPath)) {
      return memo.getOrElse(rootPath, null)
    }

    var predicateLinks = PathLinks.findPredicateLinks(graph, rootPath, wordPathMap)
    var objectLinks = PathLinks.findObjectLinks(graph, rootPath, wordPathMap)

    if (predicateLinks.nonEmpty) {
      predicateLinks = predicateLinks.map(linkPath => linkPaths(linkPath))
    }

    if (objectLinks.nonEmpty) {
      objectLinks = objectLinks.map(linkPath => linkPaths(linkPath))
    }

    val result = copyAndUpdate(rootPath, predicateLinks, objectLinks)
    memo.put(rootPath, result)

    result
  }

  private def copyAndUpdate(path: InformationPath, predicateLinks: List[InformationPath],
                            objectLinks: List[InformationPath]): InformationPath =
  {
    path.copy(predicateLinks = predicateLinks, objectLinks = objectLinks)
  }
}
