# Dependency Graph Information Extraction (DGIE), for lack of a better name
This is project was an experiment I worked on a while ago. It attemps to extract bits of information in text using only the relation of a dependency graph (and PoS tags in very rare cases). The project was rewritten in Scala, there could be few mistakes here and there while it was being rewritten.

## Motivation
The goal was to see how well a simple pattern-based approach can compete against other more complicated models. Unlike some previous attemps in using patterns, this one doesn't have long patterns for detecting cases. Every *"pattern"* is actually one step at traversing the graph and no more. The premise is that if the dependency graph was accurate enough then we can use it to segment the text into pieces of information. For example, if two words have the relation `subj` between them, then you don't need to train a model to detect that this is a relation between a subject and a predicate and work your way from there.

## Approach

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

```
find_path_from_edge(edge)
{
  (governor, dependent) = get_edge_words(edge)
  type = get_edge_type(edge)
  
  if is_subject(type)
  {
    return find_path_from_subject(governot, dependent)
  }
  else if is_object(type)
  {
    return find_path_from_object(governot, dependent)
  }
  else if is_appositional(type)
  {
    return find_path_from_apoos(governot, dependent)
  }
  else if is_clausal_modifier(type) and not is_not_relative_clausal(type)
  {
    return find_path_from_acl(governot, dependent)
  }
}

find_paths(graph)
{
  edges <- get_all_edges(graph)
  seed_edges <- filter_edges_by_relation_pattern(edges, SEED_EDGES_PATTERNS) // SEED_EDGES_PATTERNS = ['subj', 'dobj', 'acl', 'appos']
  paths <- map(seed_edges, find_path_from_edge)
}
```

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
`//TODO`

## Understanding The Output
`//TODO`

## Perfomance Compared To Other Models
Unfortunately, this project was never tested against any of the common information extraction benchmarking datasets. There just wasn't enough time. If you think the idea makes sense and you'd like to test it out, that would great.
