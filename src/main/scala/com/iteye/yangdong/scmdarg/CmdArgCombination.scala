package com.iteye.yangdong.scmdarg

import AndOrEnum._
import CmdArgRelationshipTypeEnum._

/**
 * @author Winter Young
 */
trait CmdArgCombination {
  def matches(argNames: collection.Set[String]): Boolean
  def cmdArgCombinationStr: String
  def simplify(): CmdArgCombination

  def dependsOn(cmdArgComb: CmdArgCombination): CmdArgRelationship = {
    CmdArgRelationship(DependsOn, this, cmdArgComb)
  }

  def ~> = dependsOn _

  def exclusiveFrom(cmdArgComb: CmdArgCombination): CmdArgRelationship = {
    CmdArgRelationship(ExclusiveFrom, this, cmdArgComb)
  }

  def !~> = exclusiveFrom _

  def and(comb: CmdArgCombination): CmdArgCombination = {
    StdCmdArgCombination(And, Seq(this, comb))
  }

  def or(comb: CmdArgCombination): CmdArgCombination = {
    StdCmdArgCombination(Or, Seq(this, comb))
  }
}

case class StdCmdArgCombination(operator: AndOr,
                                operatees: Seq[CmdArgCombination])
  extends CmdArgCombination {

  def matches(argNames: collection.Set[String]) = {
    var shortCircuit = false
    var result = true
    for (operatee <- operatees if !shortCircuit) {
      result = operatee.matches(argNames)
      if (operator == And && !result) {
        shortCircuit = true
      }
      else if (operator == Or && result) {
        shortCircuit = true
      }
    }
    result
  }

  def cmdArgCombinationStr = {
    val simplifiedComb = simplify()
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
      sb.append(operatee.cmdArgCombinationStr)
    }
    sb.append(")")
    sb.toString()
  }

  def simplify() = {
    val newOperatees = collection.mutable.ListBuffer[CmdArgCombination]()
    for (operatee <- operatees) {
      val simplifiedOperatee = operatee.simplify()
      simplifiedOperatee match {
        case StdCmdArgCombination(op, ops) => {
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
    StdCmdArgCombination(operator, newOperatees)
  }
}
