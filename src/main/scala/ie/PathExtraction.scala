package ie

import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphEdge}
import util.CoreNLPUtil

import scala.collection.mutable.ListBuffer

/**
  * The basic operations to extract information
  * paths and their parts.
  */
object PathExtraction {
  /**
    * Creates a predicate object out of the predicate
    * word. Finds the rest of words which make a
    * complete predicate and its auxiliary branches.
    * @param graph
    * @param word
    * @return
    */
  def predicateFromWord(graph: SemanticGraph, word: IndexedWord): Predicate = {
    Predicate(word, DependencyGraphs.findUp(graph, word, Patterns.PREDICATE_PARTS /*COMPOUND_ONLY*/),
      BranchExtraction.findAuxiliaryBranches(graph, word, Patterns.PREDICATE_AUX))
  }

  /**
    * Creates a list of words which make the full
    * object out of an object word. Finds compounds
    * and modifiers.
    * @param graph
    * @param objectEdge
    * @return
    */
  def getObjectWords(graph: SemanticGraph, objectEdge: SemanticGraphEdge): List[IndexedWord] = {
    // this is only the last word in the object words
    val objectWord = objectEdge.getDependent

    // find the rest of the words of this object
    val obj = DependencyGraphs.findCompounds(graph, objectWord)

    obj ::: DependencyGraphs.findModsAndFilter(graph, objectWord, obj)
  }

  /**
    * Finds words which are linked to an object word
    * that are actually part of the predicate.
    * @param graph
    * @param objectEdge
    * @return
    */
  def getPredicatePartsFromObject(graph: SemanticGraph, objectEdge: SemanticGraphEdge): List[AuxiliaryBranch] = {
    /*
     * find cases of the object which could be part of the predicate
     * e.g. "Side effects induced by anabolic steroid use". 'by' is
     * associated with 'use' but actually makes sense for 'induced'.
     */
    BranchExtraction.findAuxiliaryCases(graph, objectEdge.getDependent)
  }

  /**
    * Finds an auxiliary branch which for a predicate.
    * Used when a path has no object and one branch of
    * the predicate can be used to continue the sentence.
    * @param predicate
    * @return
    */
  def candidateAuxiliaryBranch(predicate: Predicate): Option[AuxiliaryBranch] = {
    for (branch <- predicate.branches) {
      val nouns = branch.words.filter(word => CoreNLPUtil.isNoun(word))

      if (nouns.nonEmpty) {
        return Some(branch)
      }
    }

    None
  }

  @deprecated
  def createPathFromSubjectFlow(subj: List[IndexedWord], predicate: Predicate,
                                obj: List[IndexedWord], indirectObj: List[IndexedWord],
                                predicateBranches: List[AuxiliaryBranch]): InformationPath =
  {
    val extendedPredicated = predicate.copy(branches = predicate.branches ++ predicateBranches)

    InformationPath(subj, extendedPredicated, obj, indirectObj)
  }


  /**
    * Creates a new predicate which contains the old
    * predicate's branches and the ones to be added.
    * @param predicate
    * @param branchesLists
    * @return
    */
  def extendPredicateBranches(predicate: Predicate, branchesLists: List[AuxiliaryBranch]*): Predicate = {
    val buffer = new ListBuffer[AuxiliaryBranch]

    buffer ++= predicate.branches
    branchesLists.foreach(list => buffer ++= list)

    predicate.copy(branches = buffer.toList)
  }

  /**
    * Finds a path out of subject and predicate words.
    * It tries to find a direct object (and an
    * indirect one if available), if none is found
    * then it finds an alternative branch from the the
    * predicate to fill its position. However, the
    * result doesn't necessarily have to contain an
    * object.
    * @param graph
    * @param subjectWord
    * @param predicateWord
    * @return
    */
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

        paths += InformationPath(subject, extendedPredicate, objWords, indirectObjectsWords)
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

        paths += InformationPath(subject, extendedPredicate, objWords, indirectObjectsWords)
      }
      else {
        paths += InformationPath(subject, predicate, List.empty, indirectObjectsWords)
      }
    }

    paths.toList
  }

  /**
    * Finds a path out of an object word and a
    * predicate word. It tries to find a subject
    * but if none is found then the subject is
    * assumed to be empty.
    * @param graph
    * @param objectWord
    * @param predicateWord
    * @return
    */
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

        buffer += InformationPath(subject, predicate, obj)
      }
    }
    else {
      buffer += InformationPath(List.empty, predicate, obj)
    }

    buffer.toList
  }

  /**
    * Does the same job as findSubjectPaths but
    * works on the case that a path starts from
    * an appositional or a clausal modifier.
    * @param graph
    * @param appsAclEdge
    * @return
    */
  def findPathsFromApposAclEdges(graph: SemanticGraph, appsAclEdge: SemanticGraphEdge): List[InformationPath] = {
    findSubjectPaths(graph, appsAclEdge.getGovernor, appsAclEdge.getDependent)
  }

  /**
    * Finds paths from an open clausal complement
    * case.
    * @param graph
    * @param xcompEdge
    * @return
    */
  def findPathsFromXcompEdge(graph: SemanticGraph, xcompEdge: SemanticGraphEdge): List[InformationPath] = {
    if (xcompEdge.getGovernor.tag().contains("VB") && !xcompEdge.getDependent.tag().contains("VB")) {
      val predicate = predicateFromWord(graph, xcompEdge.getGovernor)
      val obj = DependencyGraphs.findCompounds(graph, xcompEdge.getDependent)

      return List(InformationPath(List.empty, predicate, obj))
    }

    List.empty
  }
}
