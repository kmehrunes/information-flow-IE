package ie

import edu.stanford.nlp.simple.Document
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphFactory}
import edu.stanford.nlp.simple.Sentence

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

object InformationExtraction {

  /**
    * Finds individual unconnected information paths.
    * @param graph
    * @return
    */
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
      else if (edgeType.contains("appos")) {
        val subjectWord = edge.getGovernor
        val predicateWord = edge.getDependent

        buffer ++= PathExtraction.findSubjectPaths(graph, subjectWord, predicateWord)
      }
      else if (edgeType.contains("acl") && !edgeType.contains("relcl")) {
        val subjectWord = edge.getGovernor
        val predicateWord = edge.getDependent

        buffer ++= PathExtraction.findSubjectPaths(graph, subjectWord, predicateWord)
      }
      // TODO: check if this is needed
      else if (edgeType.contains("xcomp")) {
        println("xcomp here")
        buffer ++= PathExtraction.findPathsFromXcompEdge(graph, edge)
      }
    })

    buffer.toList
  }

  /**
    * Links individual paths to form complete meaningful ones.
    * @param graph
    * @param paths
    * @return
    */
  def linkPaths(graph: SemanticGraph, paths: List[InformationPath]): List[InformationPath] = {
    val pathsMap = PathLinks.linkWordsToPaths(paths)
    val pathLinker = new StatefulPathLinker(graph, pathsMap)

    paths.map(path => pathLinker.linkPaths(path))
  }

  /**
    * Filters out paths which are linked to the predicate of
    * another parent path. Those paths don't have a meaning
    * on their own and shouldn't exist individually.
    * @param paths
    * @return
    */
  def filterChildPredicateLinks(paths: List[InformationPath]): List[InformationPath] = {
    val childPredicateLinks = paths.flatMap(path => path.predicateLinks).toSet

    paths.filter(path => !childPredicateLinks.contains(path))
  }

  /**
    * Runs the pipeline of creating a graph, finding information
    * paths, linking them, and filtering out incomplete ones.
    * @param text
    * @return
    */
  def runPipeline(text: String): List[ExtractionResult] = {
    val sentences = new Document(text).sentences().asScala
    sentences.map(sent => ExtractionResult(sent.text(), runPipelineSentence(sent))).toList
  }

  private def runPipelineSentence(sentence: Sentence): List[InformationPath] = {
    val graph = sentence.dependencyGraph(SemanticGraphFactory.Mode.ENHANCED_PLUS_PLUS)
    val paths = InformationExtraction.findSimplePaths(graph)
    val linked = InformationExtraction.linkPaths(graph, paths)

    InformationExtraction.filterChildPredicateLinks(linked)
  }
}
