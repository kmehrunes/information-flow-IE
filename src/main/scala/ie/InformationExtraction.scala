package ie

import edu.stanford.nlp.semgraph.SemanticGraph

import scala.collection.mutable.ListBuffer

object InformationExtraction {

  def findSimplePaths(graph: SemanticGraph): List[InformationPath] = {
    val buffer = new ListBuffer[InformationPath]

    val predicateEdges = DependencyGraphs.findPredicateEdges(graph)

    predicateEdges.foreach(edge => {
      val edgeType = DependencyGraphs.getEdgeType(edge)
      println(edgeType)

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

}
