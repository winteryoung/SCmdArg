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
    AndOrCmdArgMatcher(And, Seq(this, comb))
  }

  def or(comb: CmdArgMatcher): CmdArgMatcher = {
    AndOrCmdArgMatcher(Or, Seq(this, comb))
  }
}

case class AndOrCmdArgMatcher(operator: AndOr, operatees: Seq[CmdArgMatcher])
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
        case AndOrCmdArgMatcher(op, ops) => {
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
    AndOrCmdArgMatcher(operator, newOperatees)
  }
}

case class EqualityCmdArgMatcher(argName: String, expectedArgValue: String) extends CmdArgMatcher {
  def matches(valueTable: CmdArgValueTable) = {
    var ret = false
    if (valueTable.isValueGiven(argName)) {
      val values = valueTable(argName)
      if (values.size == 1) {
        ret = valueTable(argName).find(_ == expectedArgValue).isDefined
      }
    }
    ret
  }

  def matcherString = "(--%s = %s)" format (argName, expectedArgValue)

  def simplifyMatcherTree() = this
}

case class ContainmentCmdArgMatcher(argName: String, expectedContainedValue: String) extends CmdArgMatcher {
  def matches(valueTable: CmdArgValueTable) = {
    valueTable.isValueGiven(argName) && valueTable(argName).find(_ == expectedContainedValue).isDefined
  }

  def matcherString = "(--%s contains %s)" format (argName, expectedContainedValue)

  def simplifyMatcherTree() = this
}