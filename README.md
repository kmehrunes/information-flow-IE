# Dependency Graph Information Extraction (DGIE), for lack of a better name
This is project was an experiment I worked on a while ago. It attemps to extract bits of information in text using only the relation of a dependency graph (and PoS tags in very rare cases). The project was rewritten in Scala, there could be few mistakes here and there while it was being rewritten.

## Motivation
The goal was to see how well a simple pattern-based approach can compete against other more complicated models. Unlike some previous attemps in using patterns, this one doesn't have long patterns for detecting cases. Every *"pattern"* is actually one step at traversing the graph and no more. The premise is that if the dependency graph was accurate enough then we can use it to segment the text into pieces of information. For example, if two words have the relation `subj` between them, then you don't need to train a model to detect that this is a relation between a subject and a predicate and work your way from there.

## Approach
`//TODO`

## Running The Tool
`//TODO`

## Examples
`//TODO`

## Understanding The Output
`//TODO`

## Perfomance Compared To Other Models
Unfortunately, this project was never tested against any of the common information extraction benchmarking datasets. There just wasn't enough time. If you think the idea makes sense and you'd like to test it out, that would great.
