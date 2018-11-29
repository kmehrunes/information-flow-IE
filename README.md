# Dependency Graph Information Extraction (DGIE), for lack of a better name
This is project was an experiment I worked on a while ago. It attemps to extract bits of information in text using only the relation of a dependency graph (and PoS tags in very rare cases). The project was rewritten in Scala, there could be few mistakes here and there while it was being rewritten.

To have a clear understanding and to find examples for the types of relations between words used here and in the implementation refer to:
* [Universal Dependency Relations](http://universaldependencies.org/u/dep/index.html)
* [Stanford typed dependencies manual](https://nlp.stanford.edu/software/dependencies_manual.pdf)

This project uses [Stanford CoreNLP Dependency Parser](https://nlp.stanford.edu/software/stanford-dependencies.shtml) to generate the dependency graph. You can check CoreNLP [here](https://stanfordnlp.github.io/CoreNLP/).

## Motivation
The goal was to see how well a simple pattern-based approach can compete against other more complicated models. Unlike some previous attemps in using patterns, this one doesn't have long patterns for detecting cases. Every *"pattern"* is actually one step at traversing the graph and no more. The premise is that if the dependency graph was accurate enough then we can use it to segment the text into pieces of information. For example, if two words have the relation `subj` between them, then you don't need to train a model to detect that this is a relation between a subject and a predicate and work your way from there.

## Approach
The approach is split into three phases: extracting information paths, linking the paths, and removing incomplete ones. Check the pseudocode below which ties all of them together. We'll explain each phases in the following subsections along with some pseudocode.

```
pipeline(sentence)
{
  graph <- create_dependency_graph(sentence)
  paths <- find_paths(graph)
  linked_paths <- link_paths(paths)
  filtered_paths <- remove_incomplete_paths(linked_paths)
  
  return filtered_paths
}
```

### Extracting Paths
The extraction phase has the following steps:
* Find seed edges: In this step we look for edges which could be the root of a path. The trivial method would be to look for edges which contain a subject or an object relation. However, it's a bit more complicated than that. The current implementation looks for edges where the relation could be subject (*subj*), direct object (*dobj*), appositional modifier (*appos*), or adjectal clause (*acl*). There's also one case of adjectal clauses which should be avoided, and that's a relative clause (*acl:relcl*).

* Expand the path: After finding the root edge of a path, we'll have two words to start from. For example, for a *subj* edge, we'll have one word as the subject and the other as the predicate. From those words we'll expand with finding compounds and modifier edges to complete the subject and the predicate. We'll also look into the predicate to see if there's an *obj* edge to create the object from, and the same thing for indirect object (*iobj*) edges.

* Find auxiliary branches: The predicate could have branches which give more information about it, such as when and where the action ocurred. Those branches are mainly linked to the predicate with case (*case*) or nominql modifier (*nmod*) edge.

A brief pseudocode of the process is given below but without going into the details on how each part is actually done.
```
find_paths(graph)
{
  edges <- get_all_edges(graph)
  seed_edges <- filter_edges_by_relation_pattern(edges, SEED_EDGES_PATTERNS) // SEED_EDGES_PATTERNS = ['subj', 'dobj', 'acl', 'appos']
  paths <- map(seed_edges, find_path_from_edge)
}

find_path_from_edge(edge)
{
  (governor, dependent) = get_edge_words(edge)
  type = get_edge_type(edge)
  
  if is_subject(type)
  {
    return find_path_from_subject(governor, dependent)
  }
  else if is_object(type)
  {
    return find_path_from_object(governor, dependent)
  }
  else if is_appositional(type)
  {
    return find_path_from_appos(governot, dependent)
  }
  else if is_clausal_modifier(type) and not is_not_relative_clausal(type)
  {
    return find_path_from_acl(governot, dependent)
  }
}
```

### Linking Paths
More often than not, the flow of information spans multiple paths in a sentence. The linked path could either complete the meaning of a sentence, or it adds extra information to it. Here we'll refer to the former as a predicate link, and the latter as an object link. Basically the meaning isn't complete without the predicate link, while the object link just gives more details.

Paths are linked through edges with any of the following relations: clausal complement (*ccomp*), open clausal complement (*xcomp*), adjectal clause (*acl*), adverbial clause modifier (*advcl*), referent (*ref*), or parataxis (*parataxis*). This applies to both types of links.

### Filtering Incomplete Paths
As mentioned in the previous subsection (Linking Paths), some paths exist to complete the meaning of another paths. Those paths are extentions from another path and don't make sense by themselves. As a result, they're removed at this stage to keep all paths coherent.

## Running The Tool
You can run it using `sbt "run <args>"`, but keep in mind that you may need to give SBT some extra memory so the command might look like `sbt -mem <memory> "run <args>`.

The tool could be used in one of four ways depending on the mode specified in the arguments:
* input (`--input <text>`): takes the input from the CLI directly
* interactive (`--interactive`): prompts input until user exits
* file (`--file <path to file>`): processes a text file where each line is treated as an individual document
* server (`--server`): runs a server on port 3210 which exposes a single REST endpoint `/ie`, it takes a raw text as a request body and returns the result as JSON

There are also other options, such as:
* format (`--format <plain | json>`, default `plain`): specified the format of the output (doesn't have any effect when running as a server)
* output (`--output <stdout | path to file>`, default `stdout`): specifies whether the output should be sent to standard output or to a file (doesn't have any effect when running in server or interactive modes)

## Examples
A simple single path.

**Led Zepplin was formed in 1986**
```
(subj: Led Zepplin, pred: formed[List(auxpass: was, in: 1986)])
```

A more complicated sentence which contains multiple paths and links. In face, this particular sentence was the reason why this whole project started.

**MAS CEO confirms SAR ops and says airline is working to verify speculations that the MH370 may have landed in Nanning**
```
(subj: MAS CEO, pred: confirms, obj: SAR ops)
(subj: MAS CEO, pred: says)
	predicate link: (subj: airline, pred: working[List(aux: is)])
		predicate link: (subj: airline, pred: verify, obj: speculations)
			object link: (subj: MH370, pred: landed[List(aux: may, aux: have, case: in)], obj: Nanning)
(subj: MH370, pred: landed[List(aux: may, aux: have, case: in)], obj: Nanning)
```

## Understanding The Output
The output, wether plain or in JSON format, doesn't really highlight the extracted information itself. Instead, it gives a more general representation of the input in the form of connected segments which can make up the information. For example, "Alice was born in Atlantis" will not yield the information \[Alice\] --- birth_place --> \[Atlantis\] but will yield \[Alice\] --- born (was, in) --> \[Atlantis\].

To further understand the output, here are the fields and what they represent:
* subject: the subject of the path including its adjectives and modifiers
* predicate: the predicate of the path including its negation, cases, and other information such as time and place modifiers
* object: the object of the path including its adjectives and modifiers
* indirect object: the secondary object of the path including its adjectives and modifiers
* predicate links: child paths which complete the meaning of the current path
* object links: child paths which have links to the current one and provide more information

Note that not all fields must be set for all cases.

## Perfomance Compared To Other Models
Unfortunately, this project was never tested against any of the common information extraction benchmarking datasets. There just wasn't enough time. If you think the idea makes sense and you'd like to test it out, that would great.
