package com.iteye.yangdong.scmdarg

import collection.mutable

/**
 * @author Winter Young
 */
class CmdArgTable {
  private val defTable = mutable.HashMap.empty[String, CmdArg[_]]
  private val shortNameToLongName = mutable.HashMap.empty[Char, String]
  private val valueTable = mutable.HashMap.empty[String, String]
  private val argNameList = mutable.ListBuffer[String]()
  private var defaultArgName: Option[String] = None

  def argNameSet: collection.Set[String] = {
    valueTable.keySet
  }

  def argNameSeq: Seq[String] = {
    argNameList
  }

  def validateRequiredness() {
    for (cmdArg <- defTable.values if cmdArg.isRequired) {
      if (!isValueGiven(cmdArg.argName)) {
        throw new CmdArgParserException("Missing required arg \"--%s\"" format cmdArg.argName)
      }
    }
  }

  def getValue(argName: String): String = {
    if (!valueTable.contains(argName)) {
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
    valueTable.contains(argName)
  }

  def defineArg[T](cmdArg: CmdArg[T]) {
    defTable(cmdArg.argName) = cmdArg
    argNameList += cmdArg.argName

    cmdArg.shortName match {
      case Some(s) => shortNameToLongName(s) = cmdArg.argName
      case _ =>
    }

    if (cmdArg.isBooleanCmdArg) {
      if (cmdArg.default.isDefined && cmdArg.default.get == "true") {
        throw new Exception("A boolean CmdArg can't have a true default value")
      }
      valueTable(cmdArg.argName) = "false"
    }
    else {
      cmdArg.default match {
        case Some(d) => valueTable(cmdArg.argName) = d
        case _ =>
      }
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
    val cmdArg = defTable(argName)
    cmdArg.valueConverter(argValue)

    if (cmdArg.validValueSet != None && !(cmdArg.validValueSet.get)(argValue)) {
      val possibleValues = cmdArg.validValueSet.get.mkString(", ")
      throw new CmdArgParserException(""""%s" is not a valid value for "--%s". The possible values are: (%s)""" format (argValue, argName, possibleValues))
    }

    valueTable(argName) = argValue
  }

  def getDefaultArgName: Option[String] = defaultArgName
}
