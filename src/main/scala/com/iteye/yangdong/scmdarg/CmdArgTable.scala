package com.iteye.yangdong.scmdarg

import collection.mutable

/**
 * @author Winter Young
 */
class CmdArgTable {
  private val defTable = mutable.HashMap.empty[String, CmdArg[_]]
  private val shortNameToLongName = mutable.HashMap.empty[Char, String]
  private var defaultArgName: Option[String] = None

  private var _valueTable = new CmdArgValueTable

  private[scmdarg] def valueTable = _valueTable

  def argNames = valueTable.argNames

  def clear() {
    _valueTable = new CmdArgValueTable
  }

  def validateRequiredness() {
    for (cmdArg <- defTable.values if cmdArg.isRequired) {
      if (!isValueGiven(cmdArg.argName)) {
        throw new CmdArgParserException("Missing required arg \"--%s\"" format cmdArg.argName)
      }
    }
  }

  def getValue(argName: String): Seq[String] = {
    if (!valueTable.isValueGiven(argName)) {
      throw new CmdArgParserException("Value for \"--%s\" was not given" format argName)
    }
    valueTable(argName)
  }

  def getDef(argName: String): CmdArg[_] = {
    if (!defTable.contains(argName)) {
      throw new CmdArgParserException("\"--%s\" not defined" format argName)
    }
    defTable(argName)
  }

  def getArgName(shortName: Char): String = {
    if (!shortNameToLongName.contains(shortName)) {
      throw new CmdArgParserException("\"-%s\" not defined" format shortName)
    }
    shortNameToLongName(shortName)
  }

  def isValueGiven(argName: String): Boolean = {
    valueTable.isValueGiven(argName)
  }

  def defineArg[T](cmdArg: CmdArg[T]) {
    defTable(cmdArg.argName) = cmdArg

    cmdArg.shortName match {
      case Some(s) => shortNameToLongName(s) = cmdArg.argName
      case _ =>
    }

    if (cmdArg.isDefault) {
      defaultArgName match {
        case None => defaultArgName = Some(cmdArg.argName)
        case _ => throw new CmdArgParserException("Multiple default arguments are not supported")
      }
    }
  }

  def setArgValue(argName: String, argValue: String) {
    // try to convert the value first to ensure the value conforms to the expected type
    val cmdArg = getDef(argName)
    cmdArg.validateValue(argValue)

    if (cmdArg.validValueSet != None && !(cmdArg.validValueSet.get)(argValue)) {
      val possibleValues = cmdArg.validValueSet.get.mkString(", ")
      throw new CmdArgParserException(""""%s" is not a valid value for "--%s". The possible values are: (%s)""" format (argValue, argName, possibleValues))
    }

    valueTable.addValue(argName, argValue)
  }

  def getDefaultArgName: Option[String] = defaultArgName
}
