package ie

import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphEdge}

object BranchExtraction {
  def auxiliaryInformationFromEdge(graph: SemanticGraph, edge: SemanticGraphEdge): AuxiliaryBranch = {
    val dependent = edge.getDependent
    val words = DependencyGraphs.findCompoundsAndMods(graph, dependent)

    AuxiliaryBranch(DependencyGraphs.getEdgeType(edge), words)
  }

  def findAuxiliaryBranches(graph: SemanticGraph, word: IndexedWord, patterns: List[String]): List[AuxiliaryBranch] = {
    val relatedWords = DependencyGraphs.findOutgoingEdges(graph, word, patterns)

    relatedWords.map(edge => auxiliaryInformationFromEdge(graph, edge))
  }

  def findAuxiliaryCases(graph: SemanticGraph, word: IndexedWord): List[AuxiliaryBranch] = {
    findAuxiliaryBranches(graph, word, Patterns.CASE_RELATIONS)
  }

  def findAuxiliaryNMods(graph: SemanticGraph, word: IndexedWord): List[AuxiliaryBranch] = {
    findAuxiliaryBranches(graph, word, Patterns.NMOD_RELATION)
  }
}
