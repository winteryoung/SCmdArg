package com.iteye.yangdong.scmdarg

import CmdArgRelationshipTypeEnum._

/**
 * @author Winter Young
 */
case class CmdArgRelationship(relType: CmdArgRelationshipType,
                              left: CmdArgCombination,
                              right: CmdArgCombination) {
  def verify(argNames: collection.Set[String]) {
    if (left.matches(argNames)) {
      val rightMatches = right.matches(argNames)
      relType match {
        case DependsOn if !rightMatches =>
          throw new CmdArgParserException(""""%s" require(s) "%s""""" format (left.cmdArgCombinationStr, right.cmdArgCombinationStr))
        case ExclusiveFrom if rightMatches =>
          throw new CmdArgParserException(""""%s" is/are exclusive from "%s""""" format (left.cmdArgCombinationStr, right.cmdArgCombinationStr))
        case _ =>
      }
    }
  }
}
