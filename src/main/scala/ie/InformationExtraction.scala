package ie

import edu.stanford.nlp.semgraph.SemanticGraph
import ie.PathExtraction.findSubjectPaths

import scala.collection.mutable.ListBuffer

object InformationExtraction {

  def findSimplePaths(graph: SemanticGraph): List[InformationPath] = {
    val buffer = new ListBuffer[InformationPath]

    val predicateEdges = DependencyGraphs.findPredicateEdges(graph)

    predicateEdges.foreach(edge => {
      val edgeType = DependencyGraphs.getEdgeType(edge)

      if (edgeType.contains("subj")) {
        val predicateWord = edge.getGovernor
        val subjWord = edge.getDependent

        buffer.appendAll(findSubjectPaths(graph, subjWord, predicateWord))
      }
    })

    buffer.toList
  }

}
