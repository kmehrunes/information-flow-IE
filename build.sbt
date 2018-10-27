name := "dgie"

version := "1.0"

scalaVersion := "2.12.7"

resolvers += Resolver.mavenLocal

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"
        