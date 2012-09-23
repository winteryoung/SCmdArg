package com.iteye.yangdong.scmdarg

import CmdArgRelationshipTypeEnum._

/**
 * @author Winter Young
 */
case class CmdArgRelationship(relType: CmdArgRelationshipType,
                              left: CmdArgMatcher,
                              right: CmdArgMatcher) {
  def verify(valueTable: CmdArgValueTable) {
    if (left.matches(valueTable)) {
      val rightMatches = right.matches(valueTable)
      relType match {
        case DependsOn if !rightMatches =>
          throw new CmdArgParserException(""""%s" require(s) "%s""""" format (left.matcherString, right.matcherString))
        case ExclusiveFrom if rightMatches =>
          throw new CmdArgParserException(""""%s" is/are exclusive from "%s""""" format (left.matcherString, right.matcherString))
        case _ =>
      }
    }
  }
}
