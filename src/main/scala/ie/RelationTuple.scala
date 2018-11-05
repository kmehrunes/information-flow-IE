package ie

case class RelationTuple(subj: String, predicate: String, obj: String, extra: List[String]) {
  override def toString: String = "s: " + subj + ", p: " + predicate + ", o: " + obj + ", extra: {" + "}"
}
