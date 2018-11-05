package ie

import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphFactory}
import edu.stanford.nlp.simple.Sentence

import scala.collection.mutable.ListBuffer

object InformationExtraction {

  def findSimplePaths(graph: SemanticGraph): List[InformationPath] = {
    val buffer = new ListBuffer[InformationPath]

    val predicateEdges = DependencyGraphs.findPredicateEdges(graph)

    predicateEdges.foreach(edge => {
      val edgeType = DependencyGraphs.getEdgeType(edge)

      if (edgeType.contains("subj")) {
        val predicateWord = edge.getGovernor
        val subjectWord = edge.getDependent

        buffer ++= PathExtraction.findSubjectPaths(graph, subjectWord, predicateWord)
      }
      else if (edgeType.contains("obj")) {
        val predicateWord = edge.getGovernor
        val objectWord = edge.getDependent

        buffer ++= PathExtraction.findObjectPaths(graph, objectWord, predicateWord)
      }
      else if (edgeType.contains("appos") || edgeType.contains("acl")) {
        val subjectWord = edge.getGovernor
        val predicateWord = edge.getDependent

        buffer ++= PathExtraction.findSubjectPaths(graph, subjectWord, predicateWord)
      }
      // TODO: check if this is needed
      else if (edgeType.contains("xcomp")) {
        buffer ++= PathExtraction.findPathsFromXcompEdge(graph, edge)
      }
    })

    buffer.toList
  }

  def linkPaths(graph: SemanticGraph, paths: List[InformationPath]): List[InformationPath] = {
    val pathsMap = PathLinks.linkWordsToPaths(paths)
    val pathLinker = new StatefulPathLinker(graph, pathsMap)

    paths.map(path => pathLinker.linkPaths(path))
  }

  def filterChildPredicateLinks(paths: List[InformationPath]): List[InformationPath] = {
    val childPredicateLinks = paths.flatMap(path => path.predicateLinks).toSet

    paths.filter(path => !childPredicateLinks.contains(path))
  }

  def runPipeline(sentence: String): List[InformationPath] = {
    val graph = new Sentence(sentence).dependencyGraph(SemanticGraphFactory.Mode.ENHANCED_PLUS_PLUS)
    val paths = InformationExtraction.findSimplePaths(graph)
    val linked = InformationExtraction.linkPaths(graph, paths)

    InformationExtraction.filterChildPredicateLinks(linked)
  }
}
