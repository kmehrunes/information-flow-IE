package ie

import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphEdge}
import util.StringUtil

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object DependencyGraphs {
  private def verifyEdgeRelationName(edge: SemanticGraphEdge, patterns: List[String]): Boolean = {
    StringUtil.containsAny(edge.getRelation.getShortName, patterns)
  }

  private def childWordToEdge(parent: IndexedWord, child: IndexedWord, graph: SemanticGraph): SemanticGraphEdge = {
    graph.getEdge(parent, child)
  }

  private def isConnectingWord(graph: SemanticGraph, parent:IndexedWord, child: IndexedWord): Boolean = {
    for (edge: SemanticGraphEdge <- graph.getAllEdges(parent, child)) {
      if (StringUtil.containsAny(edge.getRelation.getLongName, Patterns.INTERPATH_RELATIONS)) {
        return true
      }
    }
    false
  }

  def findOutgoingEdges(graph: SemanticGraph, start: IndexedWord, contains: List[String]): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]

    for (edge <- graph.outgoingEdgeList(start)) {
      if (verifyEdgeRelationName(edge, contains)) {
        buffer += edge
      }
    }

    buffer.toList
  }

  def findMatchingChildrenEdges(graph: SemanticGraph, parent: IndexedWord, patterns: List[String]): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]

    for (child <- graph.getChildList(parent)) {
      val edge = childWordToEdge(parent, child, graph)
      if (verifyEdgeRelationName(edge, patterns)) {
        buffer += edge
      }
    }

    buffer.toList
  }

  def findUp(graph: SemanticGraph, start: IndexedWord, patterns: List[String]): List[IndexedWord] = {
    val buffer = new ListBuffer[IndexedWord]
    val wordParents = graph.outgoingEdgeList(start)

    for (edge: SemanticGraphEdge <- wordParents) {
      if (verifyEdgeRelationName(edge, patterns)) {
        buffer += edge.getDependent
      }
    }

    buffer.toList
  }

  def findUpExtendedCases(graph: SemanticGraph, start: IndexedWord, patterns: List[String]): List[IndexedWord] = {
    val buffer = new ListBuffer[IndexedWord]
    val wordParents = graph.outgoingEdgeList(start)

    for (edge: SemanticGraphEdge <- wordParents) {
      if (verifyEdgeRelationName(edge, patterns)) {
        val dependent = edge.getDependent
        val cases = findUp(graph, dependent, Patterns.EXTENDED_CASE_RELATIONS)

        buffer ++= cases
        buffer += dependent
      }
    }

    buffer.toList
  }

  def filterWords(words: List[IndexedWord], wordsToRemove: List[IndexedWord]): List[IndexedWord] = {
    words.filter(word => wordsToRemove.contains(word))
  }

  def getEdgeType(edge: SemanticGraphEdge): String = {
    if (edge.getRelation.getSpecific != null) edge.getRelation.getSpecific
    else edge.getRelation.getShortName
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

  def findCompounds(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    findUp(graph, start, Patterns.COMPOUND_AND_JMODS) :+ start
  }

  def findMods(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    findUpExtendedCases(graph, start, Patterns.MODS)
  }

  def findModsAndFilter(graph: SemanticGraph, start: IndexedWord, wordsToRemove: List[IndexedWord]): List[IndexedWord] = {
    filterWords(findMods(graph, start), wordsToRemove)
  }

  def findCompoundsAndMods(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    val compounds = findCompounds(graph, start)
    compounds ++ findModsAndFilter(graph, start, compounds)
  }

  def findConnectingRelations(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    val buffer = new ListBuffer[IndexedWord]
    val children = graph.getChildList(start)

    for (child <- children) {
      if (isConnectingWord(graph, start, child)) {
        buffer += child
      }
    }

    buffer.toList
  }
}
