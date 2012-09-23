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

  def dependsOn(cmdArgComb: CmdArgMatcher): Seq[CmdArgRelationship] = {
    Seq(CmdArgRelationship(DependsOn, this, cmdArgComb))
  }

  def ~> = dependsOn _

  def <~>(cmdArgMatcher: CmdArgMatcher): Seq[CmdArgRelationship] = {
    (this ~> cmdArgMatcher) ++ (cmdArgMatcher ~> this)
  }

  def exclusiveFrom(cmdArgComb: CmdArgMatcher): Seq[CmdArgRelationship] = {
    Seq(CmdArgRelationship(ExclusiveFrom, this, cmdArgComb))
  }

  def !~> = exclusiveFrom _

  def <~!~>(cmdArgMatcher: CmdArgMatcher): Seq[CmdArgRelationship] = {
    (this !~> cmdArgMatcher) ++ (cmdArgMatcher !~> this)
  }

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

case class EqualityCmdArgMatcher(argName: String,
                                 expectedArgValue: String,
                                 cmp: (String, String) => Boolean = _ == _) extends CmdArgMatcher {
  def matches(valueTable: CmdArgValueTable) = {
    var ret = false
    if (valueTable.isValueGiven(argName)) {
      val values = valueTable(argName)
      if (values.size == 1) {
        ret = valueTable(argName).find(cmp(_, expectedArgValue)).isDefined
      }
    }
    ret
  }

  def matcherString = "(--%s = %s)" format (argName, expectedArgValue)

  def simplifyMatcherTree() = this
}

case class ContainmentCmdArgMatcher(argName: String,
                                    expectedContainedValue: String,
                                    cmp: (String, String) => Boolean = _ == _) extends CmdArgMatcher {
  def matches(valueTable: CmdArgValueTable) = {
    valueTable.isValueGiven(argName) && valueTable(argName).find(cmp(_, expectedContainedValue)).isDefined
  }

  def matcherString = "(--%s contains %s)" format (argName, expectedContainedValue)

  def simplifyMatcherTree() = this
}