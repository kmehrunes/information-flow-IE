name := "dgie"

version := "1.0"

scalaVersion := "2.12.7"

resolvers += Resolver.mavenLocal

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.21"

libraryDependencies += "org.rogach" %% "scallop" % "3.1.3"

libraryDependencies += "org.json4s" % "json4s-core_2.12" % "3.6.2"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.2"

libraryDependencies += "io.javalin" % "javalin" % "2.4.0"