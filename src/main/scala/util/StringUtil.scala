package util

object StringUtil {
  def containsAny(str: String, matches: List[String]): Boolean = {
    val regex = ".*(" + matches.mkString("|") + ").*"
    str.matches(regex)
  }
}
