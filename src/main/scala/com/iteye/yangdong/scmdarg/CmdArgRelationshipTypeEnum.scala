package com.iteye.yangdong.scmdarg

/**
 * @author Winter Young
 */
object CmdArgRelationshipTypeEnum extends Enumeration {
  type CmdArgRelationshipType = Value
  val DependsOn, ExclusiveFrom = Value
}
