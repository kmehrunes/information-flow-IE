package ie

import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphEdge}
import util.CoreNLPUtil

import scala.collection.mutable.ListBuffer

object PathExtraction {
  def predicateFromWord(graph: SemanticGraph, word: IndexedWord): Predicate = {
    Predicate(word, DependencyGraphs.findUp(graph, word, Patterns.PREDICATE_PARTS /*COMPOUND_ONLY*/),
      BranchExtraction.findAuxiliaryBranches(graph, word, Patterns.PREDICATE_AUX))
  }

  def getObjectWords(graph: SemanticGraph, objectEdge: SemanticGraphEdge): List[IndexedWord] = {
    // this is only the last word in the object words
    val objectWord = objectEdge.getDependent

    // find the rest of the words of this object
    val obj = DependencyGraphs.findCompounds(graph, objectWord)

    obj ::: DependencyGraphs.findModsAndFilter(graph, objectWord, obj)
  }

  def getPredicatePartsFromObject(graph: SemanticGraph, objectEdge: SemanticGraphEdge): List[AuxiliaryBranch] = {
    /*
     * find cases of the object which could be part of the predicate
     * e.g. "Side effects induced by anabolic steroid use". 'by' is
     * associated with 'use' but actually makes sense for 'induced'.
     */
    BranchExtraction.findAuxiliaryCases(graph, objectEdge.getDependent)
  }

  def candidateAuxiliaryBranch(predicate: Predicate): Option[AuxiliaryBranch] = {
    for (branch <- predicate.branches) {
      val nouns = branch.words.filter(word => CoreNLPUtil.isNoun(word))

      if (nouns.nonEmpty) {
        return Some(branch)
      }
    }

    None
  }

  def createPathFromSubjectFlow(subj: List[IndexedWord], predicate: Predicate,
                                obj: List[IndexedWord], indirectObj: List[IndexedWord],
                                predicateBranches: List[AuxiliaryBranch]): InformationPath =
  {
    val extendedPredicated = predicate.copy(branches = predicate.branches ++ predicateBranches)

    InformationPath(subj, extendedPredicated, obj, indirectObj, List(), List())
  }

  def extendPredicateBranches(predicate: Predicate, branchesLists: List[AuxiliaryBranch]*): Predicate = {
    val buffer = new ListBuffer[AuxiliaryBranch]

    buffer ++= predicate.branches
    branchesLists.foreach(list => buffer ++= list)

    predicate.copy(branches = buffer.toList)
  }

  def findSubjectPaths(graph: SemanticGraph, subjectWord: IndexedWord,
                       predicateWord: IndexedWord): List[InformationPath] =
  {
    val paths = new ListBuffer[InformationPath]

    val subject = DependencyGraphs.findCompoundsAndMods(graph, subjectWord)
    val predicate = predicateFromWord(graph, predicateWord)

    val directObjectsEdges = DependencyGraphs.findDirectObjects(graph, predicateWord)
    val indirectObjectsEdges = DependencyGraphs.findIndirectObjects(graph, predicateWord)

    val indirectObjectsWords = indirectObjectsEdges.map(edge => edge.getDependent)

    if (directObjectsEdges.nonEmpty) {
      // here we expand objects and branches
      for (edge <- directObjectsEdges) {
        val objWords = getObjectWords(graph, edge)
        val objWord = objWords.last

        val caseBranches = BranchExtraction.findAuxiliaryCases(graph, edge.getDependent)
        val nmodBranches = BranchExtraction.findAuxiliaryNMods(graph, objWord)

        val extendedPredicate = extendPredicateBranches(predicate, caseBranches, nmodBranches)

        paths += InformationPath(subject, extendedPredicate, objWords, indirectObjectsWords, List.empty, List.empty)
      }
    }
    else {
      val objBranchOpt = candidateAuxiliaryBranch(predicate)

      if (objBranchOpt.isDefined) {
        val objBranch = objBranchOpt.get
        val objWords = objBranch.words
        val lastWord = objWords.last

        val filteredBranches = predicate.branches.filter(branch => branch != objBranch)
        val auxiliaryCaseBranches = BranchExtraction.findAuxiliaryCases(graph, lastWord)

        val extendedPredicate = predicate.copy(branches = filteredBranches ++ auxiliaryCaseBranches)

        paths += InformationPath(subject, extendedPredicate, objWords, indirectObjectsWords, List.empty, List.empty)
      }
      else {
        paths += InformationPath(subject, predicate, List.empty, indirectObjectsWords, List.empty, List.empty)
      }
    }

    paths.toList
  }

  def findObjectPaths(graph: SemanticGraph, objectWord: IndexedWord,
                      predicateWord: IndexedWord): List[InformationPath] =
  {
    val buffer = new ListBuffer[InformationPath]

    val obj = DependencyGraphs.findCompoundsAndMods(graph, objectWord)
    val predicate = predicateFromWord(graph, predicateWord)

    val directSubjectsEdges = DependencyGraphs.findDirectSubjects(graph, predicateWord)

    if (directSubjectsEdges.nonEmpty) {
      for (subjectEdge <- directSubjectsEdges) {
        val subjectWord = subjectEdge.getDependent
        val subject = DependencyGraphs.findCompoundsAndMods(graph, subjectWord)

        buffer += InformationPath(subject, predicate, obj, List.empty, List.empty, List.empty)
      }
    }
    else {
      buffer += InformationPath(List.empty, predicate, obj, List.empty, List.empty, List.empty)
    }

    buffer.toList
  }

  def findPathsFromApposAclEdges(graph: SemanticGraph, appsAclEdge: SemanticGraphEdge): List[InformationPath] = {
    findSubjectPaths(graph, appsAclEdge.getGovernor, appsAclEdge.getDependent)
  }
}
