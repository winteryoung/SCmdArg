package com.iteye.yangdong.scmdarg

import AndOrEnum._
import CmdArgRelationshipTypeEnum._

/**
 * @author Winter Young
 */
trait CmdArgMatcher {
  def matches(valueTable: CmdArgValueTable): Boolean
  def matcherString: String
  def simplifyMatcherTree(): CmdArgMatcher

  def dependsOn(cmdArgComb: CmdArgMatcher): CmdArgRelationship = {
    CmdArgRelationship(DependsOn, this, cmdArgComb)
  }

  def ~> = dependsOn _

  def exclusiveFrom(cmdArgComb: CmdArgMatcher): CmdArgRelationship = {
    CmdArgRelationship(ExclusiveFrom, this, cmdArgComb)
  }

  def !~> = exclusiveFrom _

  def and(comb: CmdArgMatcher): CmdArgMatcher = {
    StdCmdArgMatcher(And, Seq(this, comb))
  }

  def or(comb: CmdArgMatcher): CmdArgMatcher = {
    StdCmdArgMatcher(Or, Seq(this, comb))
  }
}

case class StdCmdArgMatcher(operator: AndOr, operatees: Seq[CmdArgMatcher])
  extends CmdArgMatcher {

  def matches(valueTable: CmdArgValueTable) = {
    var shortCircuit = false
    var result = true
    for (operatee <- operatees if !shortCircuit) {
      result = operatee.matches(valueTable)
      if (operator == And && !result) {
        shortCircuit = true
      }
      else if (operator == Or && result) {
        shortCircuit = true
      }
    }
    result
  }

  def matcherString = {
    val simplifiedComb = simplifyMatcherTree()
    val sep = operator match {
      case And => " and "
      case Or => " or "
    }
    var metFirst = false
    val sb = new StringBuilder()
    sb.append("(")
    for (operatee <- simplifiedComb.operatees) {
      if (metFirst) {
        sb.append(sep)
      }
      metFirst = true
      sb.append(operatee.matcherString)
    }
    sb.append(")")
    sb.toString()
  }

  def simplifyMatcherTree() = {
    val newOperatees = collection.mutable.ListBuffer[CmdArgMatcher]()
    for (operatee <- operatees) {
      val simplifiedOperatee = operatee.simplifyMatcherTree()
      simplifiedOperatee match {
        case StdCmdArgMatcher(op, ops) => {
          if (op == operator) {
            newOperatees ++= ops
          }
          else {
            newOperatees += simplifiedOperatee
          }
        }
        case _ => newOperatees += simplifiedOperatee
      }
    }
    StdCmdArgMatcher(operator, newOperatees)
  }
}
