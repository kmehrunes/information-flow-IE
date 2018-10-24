package ie

object Patterns {
  val COMPOUND_ONLY = List("compound")
  val COMPOUND_AND_MODS = List("compound", "nmod", "amod", "nummod")
  val COMPOUND_AND_JMODS = List("compound", "amod", "nummod", "nmod:npmod", "nmod:poss", "nmod:of")
  val MODS = List("nmod")
  val COMPOUND_MODS_AND_CONJ = List("compound", "nmod", "cc", "conj")
  val SUBJECT_RELATION = List("subj")
  val NMOD_RELATION = List("nmod")
  // TODO: nmod isn't always an indication of an object, should be its own case
  val OBJECT_NMOD_RELATIONS = List("obj", "nmod")
  val DIRECT_OBJECT_RELATIONS = List("dobj")
  val INDIRECT_OBJECT_RELATIONS = List("iobj")
  //---------------------------------------------------------------------------
  val SUBJECT_OBJECT = List("subj", "dobj", "acl", "appos") // iobj isn't a relation by itself

  val INTERPATH_RELATIONS = List("xcomp", "ccomp", "acl", "ref", "advcl", "parataxis")
  val PREDICATE_AUX = List("nmod", "aux", "adv", "neg")
  val PREDICATE_PARTS = List("compound", "cop", "case")
  val COP_RELATIONS = List("cop")
  val CASE_RELATIONS = List("case")
}
