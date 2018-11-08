package ie

import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphEdge}
import util.StringUtil

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import scala.collection.JavaConverters._

/**
  * A collection of various operations on semantic
  * dependency graphs. They mainly focus on traversal
  * and extracting small pieces of information from
  * a given graph.
  */
object DependencyGraphs {

  /**
    * Checks whether an edge type matches any of the
    * given patterns.
    * @param edge
    * @param patterns
    * @return
    */
  private def verifyEdgeRelationName(edge: SemanticGraphEdge, patterns: List[String]): Boolean = {
    StringUtil.containsAny(edge.getRelation.getShortName, patterns)
  }

  /**
    * Finds the edge between a parent word and a child word
    * @param parent
    * @param child
    * @param graph
    * @return
    */
  private def childWordToEdge(parent: IndexedWord, child: IndexedWord, graph: SemanticGraph): SemanticGraphEdge = {
    graph.getEdge(parent, child)
  }

  /**
    * Checks whether the child word could be considered a
    * connecting word between the parent's path and the
    * child's path. It scans all edges between the two words
    * to find a possible connection.
    * @param graph
    * @param parent
    * @param child
    * @return
    */
  private def isConnectingWord(graph: SemanticGraph, parent:IndexedWord, child: IndexedWord): Boolean = {
    for (edge: SemanticGraphEdge <- graph.getAllEdges(parent, child).asScala) {
      if (StringUtil.containsAny(edge.getRelation.getShortName, Patterns.INTERPATH_RELATIONS)) {
        return true
      }
    }
    false
  }


  /**
    * Finds all outgoing edges from a word. The edges must match
    * one of the given patterns.
    * @param graph
    * @param start
    * @param contains
    * @return
    */
  def findOutgoingEdges(graph: SemanticGraph, start: IndexedWord, contains: List[String]): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]

    for (edge <- graph.outgoingEdgeList(start).asScala) {
      if (verifyEdgeRelationName(edge, contains)) {
        buffer += edge
      }
    }

    buffer.toList
  }

  /**
    * Finds all edges between a parent word and its child words
    * where the edge type matches one of the patterns.
    * @param graph
    * @param parent
    * @param patterns
    * @return
    */
  def findMatchingChildrenEdges(graph: SemanticGraph, parent: IndexedWord, patterns: List[String]): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]

    for (child <- graph.getChildList(parent).asScala) {
      val edge = childWordToEdge(parent, child, graph)
      if (verifyEdgeRelationName(edge, patterns)) {
        buffer += edge
      }
    }

    buffer.toList
  }

  /**
    * Finds parents of a word where the edge type between the
    * two matches one of the patterns.
    * @param graph
    * @param start
    * @param patterns
    * @return
    */
  def findUp(graph: SemanticGraph, start: IndexedWord, patterns: List[String]): List[IndexedWord] = {
    val buffer = new ListBuffer[IndexedWord]
    val wordParents = graph.outgoingEdgeList(start)

    for (edge: SemanticGraphEdge <- wordParents.asScala) {
      if (verifyEdgeRelationName(edge, patterns)) {
        buffer += edge.getDependent
      }
    }

    buffer.toList
  }

  /**
    * Finds the parents of a word where the edge between
    * the two matches one of the patterns, and extends it
    * with the case words of the parent.
    * @param graph
    * @param start
    * @param patterns
    * @return
    */
  def findUpExtendedCases(graph: SemanticGraph, start: IndexedWord, patterns: List[String]): List[IndexedWord] = {
    val buffer = new ListBuffer[IndexedWord]
    val wordParents = graph.outgoingEdgeList(start)

    for (edge: SemanticGraphEdge <- wordParents.asScala) {
      if (verifyEdgeRelationName(edge, patterns)) {
        val dependent = edge.getDependent
        val cases = findUp(graph, dependent, Patterns.EXTENDED_CASE_RELATIONS)

        buffer ++= cases
        buffer += dependent
      }
    }

    buffer.toList
  }

  /**
    * No need for documentation.
    * @param words
    * @param wordsToRemove
    * @return
    */
  def filterWords(words: List[IndexedWord], wordsToRemove: List[IndexedWord]): List[IndexedWord] = {
    words.filter(word => wordsToRemove.contains(word))
  }

  /**
    * Gets the specific relation name if available, or
    * the short relation name otherwise.
    * @param edge
    * @return
    */
  def getEdgeType(edge: SemanticGraphEdge): String = {
    if (edge.getRelation.getSpecific != null) edge.getRelation.getSpecific
    else edge.getRelation.getShortName
  }

  /**
    * Finds words which are linked to a predicate word
    * with an object relation.
    * @param graph
    * @param predicate
    * @return
    */
  def findDirectObjects(graph: SemanticGraph, predicate: IndexedWord): List[SemanticGraphEdge] = {
    findMatchingChildrenEdges(graph, predicate,Patterns.DIRECT_OBJECT_RELATIONS)
  }

  /**
    * Finds words which are linked a predicate word
    * with an indirect object relation.
    * @param graph
    * @param predicate
    * @return
    */
  def findIndirectObjects(graph: SemanticGraph, predicate: IndexedWord): List[SemanticGraphEdge] = {
    findOutgoingEdges(graph, predicate, Patterns.INDIRECT_OBJECT_RELATIONS)
  }

  /**
    * Finds words which are linked to a predicate word
    * with a subject relation.
    * @param graph
    * @param predicate
    * @return
    */
  def findDirectSubjects(graph: SemanticGraph, predicate: IndexedWord): List[SemanticGraphEdge] = {
    findMatchingChildrenEdges(graph, predicate, Patterns.SUBJECT_RELATION)
  }

  /**
    * Finds all edges in a graph which could contain a
    * predicate word. A predicate word is any word which
    * has children with subject or object relation.
    * @param graph
    * @return
    */
  def findPredicateEdges(graph: SemanticGraph): List[SemanticGraphEdge] = {
    val buffer = new ListBuffer[SemanticGraphEdge]
    val found = new mutable.HashSet[Int]

    for (edge: SemanticGraphEdge <- graph.edgeIterable().asScala) {
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

  /**
    * Finds compound words which make up the rest of
    * the word for a complete meaning.
    * @param graph
    * @param start
    * @return
    */
  def findCompounds(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    findUp(graph, start, Patterns.COMPOUND_AND_JMODS) :+ start
  }

  /**
    * Finds words (with their cases) which have any
    * modifier relation to the starting word.
    * @param graph
    * @param start
    * @return
    */
  def findMods(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    findUpExtendedCases(graph, start, Patterns.MODS)
  }

  /**
    * Finds words (with their cases) which have any
    * modifier relation to the starting word. It also
    * filters out words if needed.
    * @param graph
    * @param start
    * @param wordsToRemove
    * @return
    */
  def findModsAndFilter(graph: SemanticGraph, start: IndexedWord, wordsToRemove: List[IndexedWord]): List[IndexedWord] = {
    filterWords(findMods(graph, start), wordsToRemove)
  }

  /**
    * A combination of findCompounds() and findMods().
    * @param graph
    * @param start
    * @return
    */
  def findCompoundsAndMods(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    val compounds = findCompounds(graph, start)
    compounds ++ findModsAndFilter(graph, start, compounds)
  }

  /**
    * Finds all possible connections from a word to
    * another word in a different path. Used to link
    * paths together to create a flow.
    * @param graph
    * @param start
    * @return
    */
  def findConnectingRelations(graph: SemanticGraph, start: IndexedWord): List[IndexedWord] = {
    val buffer = new ListBuffer[IndexedWord]
    val children = graph.getChildList(start)

    for (child <- children.asScala) {
      if (isConnectingWord(graph, start, child)) {
        buffer += child
      }
    }

    buffer.toList
  }
}
