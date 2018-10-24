package ie

import java.util.stream.Collectors

import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphEdge}
import util.StringUtil

import collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object DependencyGraphs {
  private def edgesFilter(edge: SemanticGraphEdge, patterns: List[String]): Boolean = {
    StringUtil.containsAny(edge.getRelation.getShortName, patterns)
  }

  private def childWordToEdge(parent: IndexedWord, child: IndexedWord, graph: SemanticGraph): SemanticGraphEdge = {
    graph.getEdge(parent, child)
  }

  def findOutgoingEdges(graph: SemanticGraph, start: IndexedWord, contains: List[String]): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]

    for (edge <- graph.outgoingEdgeList(start)) {
      if (edgesFilter(edge, contains)) {
        buffer += edge
      }
    }

    buffer.toList
  }

  def findMatchingChildrenEdges(graph: SemanticGraph, parent: IndexedWord, patterns: List[String]): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]

    for (child <- graph.getChildList(parent)) {
      val edge = childWordToEdge(parent, child, graph)
      if (edgesFilter(edge, patterns)) {
        buffer += edge
      }
    }

    buffer.toList
  }

  def findDirectObjects(graph: SemanticGraph, predicate: IndexedWord): List[SemanticGraphEdge] = {
    findMatchingChildrenEdges(graph, predicate,Patterns.DIRECT_OBJECT_RELATIONS)
  }

  def findIndirectObjects(graph: SemanticGraph, predicate: IndexedWord): List[SemanticGraphEdge] = {
    findOutgoingEdges(graph, predicate, Patterns.INDIRECT_OBJECT_RELATIONS)
  }

  def findDirectSubjects(graph: SemanticGraph, predicate: IndexedWord): List[SemanticGraphEdge] = {
    findMatchingChildrenEdges(graph, predicate, Patterns.SUBJECT_RELATION)
  }

  def findPredicateEdges(graph: SemanticGraph): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]
    val found = new mutable.HashSet[Int]

    for (edge: SemanticGraphEdge <- graph.edgeIterable()) {
      val relation = edge.getRelation.getShortName

      if (StringUtil.containsAny(relation, Patterns.SUBJECT_OBJECT)) {
        val predicate = edge.getGovernor

        if (!found.contains(predicate.index())) {
          buffer += edge
          found += predicate.index()
        }
      }
    }

    buffer.toList
  }
}
