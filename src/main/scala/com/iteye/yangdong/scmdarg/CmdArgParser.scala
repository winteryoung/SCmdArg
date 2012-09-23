package com.iteye.yangdong.scmdarg

import java.io.{PrintWriter, Writer}
import collection.mutable

/**
 * @author Winter Young
 */
abstract class CmdArgParser(appName: String, appDesc: String = "") {
  private val cmdArgTable = new CmdArgTable
  private val cmdArgRelationships = mutable.ListBuffer[CmdArgRelationship]()

  private val cmdArgNameList = mutable.LinkedHashSet[String]()

  private var autoExit = true
  private var error = false
  private var defined = false

  private var _outWriter: Writer = new PrintWriter(System.out)
  private var _errWriter: Writer = new PrintWriter(System.err)

  protected val helpArg = arg[Boolean](desc = "Display this help message", shortName = Some('h'))

  private val newline = System.getProperty("line.separator")

  private def reset() {
    cmdArgTable.clear()
    error = false
  }

  def setOutWriter(writer: Writer) {
    _outWriter = writer
  }

  def setErrWriter(writer: Writer) {
    _errWriter = writer
  }

  def setAutoExit(autoExit: Boolean) {
    this.autoExit = autoExit
  }

  def hasError: Boolean = error

  def parse(args: Array[String]) {
    try {
      parse0(args)

      if (helpArg.get) {
        displayHelp()

        if (autoExit) {
          sys.exit(0)
        }
      }
    }
    catch {
      case ex: CmdArgParserException => {
        error = true
        _errWriter.write(ex.getMessage + newline)
        _errWriter.flush()
        if (autoExit) {
          displayHelp()
          sys.exit(1)
        }
      }
    }
  }

  def defineArgs() {
    if (!defined) {
      for (field <- this.getClass.getDeclaredFields) {
        val fieldName = field.getName

        if (cmdArgNameList.contains(fieldName)) {
          throw new Exception("Duplicated definition for --" + fieldName)
        }
        cmdArgNameList += fieldName

        field.setAccessible(true)
        val cmdArg = field.get(this).asInstanceOf[CmdArg[_]]
        cmdArg.argName = fieldName

        cmdArgTable.defineArg(cmdArg)
      }
      defined = true
    }
  }

  private[scmdarg] def parse0(args: Array[String]) {
    reset()
    defineArgs()
    fillArgValues(args)
    validate()
  }

  private def displayArgsDescription() {
    _outWriter.write("OPTIONS" + newline + newline)
    for (argName <- cmdArgNameList) {
      val cmdArg = cmdArgTable.getDef(argName)
      val valueName = cmdArg.valueName

      if (cmdArg.shortName != None) {
        _outWriter.write("-%s %s" format (cmdArg.shortName.get, valueName))
        _outWriter.write(newline)
      }
      _outWriter.write("--%s %s" format (argName, valueName))
      _outWriter.write(newline)

      _outWriter.write("   " + cmdArg.desc + newline)
      if (cmdArg.isRequired) {
        _outWriter.write("   > Is required." + newline)
      }
      if (cmdArg.isDefault) {
        _outWriter.write("   > Is default." + newline)
      }
      if (cmdArg.default != None) {
        _outWriter.write("   > Default value: " + cmdArg.default.get + newline)
      }
    }
  }

  def displayHelp() {
    _outWriter.write("%s: %s" format (appName, appDesc))
    _outWriter.write(newline)
    _outWriter.write(newline)
    _outWriter.write("Usage: %s [OPTIONS]" format appName)
    _outWriter.write(newline)
    _outWriter.write(newline)
    displayArgsDescription()
    _outWriter.flush()
  }

  private def validate() {
    cmdArgTable.validateRequiredness()

    for (rel <- cmdArgRelationships) {
      rel.verify(cmdArgTable.valueTable)
    }
  }

  private def fillArgValues(realArgs: Array[String]) {
    val realArgStack = mutable.Stack(realArgs: _*)
    while (!realArgStack.isEmpty && !realArgStack.top.trim().isEmpty) {
      val realArg = realArgStack.pop()
      if (realArg.startsWith("--") && realArg.length > 2) {
        parseLongNameArg(realArg, realArgStack)
      }
      else if (realArg.startsWith("-") && realArg.length > 1) {
        parseShortNameArg(realArg, realArgStack)
      }
      else {
        cmdArgTable.getDefaultArgName match {
          case Some(d) => cmdArgTable.setArgValue(d, realArg)
          case None =>
        }
      }
    }

    for (argName <- cmdArgNameList) {
      val cmdArg = cmdArgTable.getDef(argName)
      cmdArg.default match {
        case Some(d) => {
          if (cmdArg.isBooleanCmdArg && d == "true") {
            throw new Exception("true is not allowed as a default value for boolean arguments")
          }
          if (!cmdArgTable.isValueGiven(argName)) {
            cmdArgTable.setArgValue(argName, d)
          }
        }
        case None => {
          if (cmdArg.isBooleanCmdArg) {
            cmdArgTable.setArgValue(argName, "false")
          }
        }
      }
    }
  }

  private def parseShortNameArg(realArg: String, realArgStack: mutable.Stack[String]) {
    val shortNameRealArg = mutable.Stack(realArg.substring(1): _*)
    while (!shortNameRealArg.isEmpty) {
      val shortName = shortNameRealArg.pop()
      val argName = cmdArgTable.getArgName(shortName)
      val cmdArg = cmdArgTable.getDef(argName)
      if (cmdArg.isBooleanCmdArg) {
        cmdArgTable.setArgValue(argName, "true")
      }
      else {
        var argValue = ""
        if (shortNameRealArg.isEmpty) {
          argValue = realArgStack.pop()
        }
        else {
          argValue = shortNameRealArg.foldLeft("")(_ + _)
          shortNameRealArg.clear()
        }
        cmdArgTable.setArgValue(argName, argValue)
      }
    }
  }

  private def parseLongNameArg(realArg: String, realArgStack: mutable.Stack[String]) {
    val argName = realArg.substring(2)
    val cmdArg = cmdArgTable.getDef(argName)
    if (cmdArg.isBooleanCmdArg) {
      cmdArgTable.setArgValue(argName, "true")
    }
    else {
      if (realArgStack.isEmpty) {
        throw new CmdArgParserException("Argument value not found for --" + argName)
      }
      val argValue = realArgStack.pop()
      cmdArgTable.setArgValue(argName, argValue)
    }
  }

  def arg[T](desc: String = "",
             shortName: Option[Char] = None,
             valueName: String = "",
             isRequired: Boolean = false,
             isDefault: Boolean = false,
             default: Option[String] = None,
             validValueSet: Option[Set[String]] = None)
            (implicit valueConverter: CmdArgValueConverter[T],
                      classManifestOfT: ClassManifest[T]): SingleValueCmdArg[T] = {
    SingleValueCmdArg(cmdArgTable, "#Undefined#", desc, shortName, valueName, isRequired, isDefault, default, validValueSet = validValueSet)
  }

  def marg[T](desc: String = "",
              shortName: Option[Char] = None,
              valueName: String = "",
              isRequired: Boolean = false,
              isDefault: Boolean = false,
              default: Option[String] = None,
              validValueSet: Option[Set[String]] = None)
             (implicit valueConverter: CmdArgValueConverter[T]): MultiValueCmdArg[T] = {
    MultiValueCmdArg(cmdArgTable, "#Undefined#", desc, shortName, valueName, isRequired, isDefault, default, validValueSet = validValueSet)
  }

  def rel(argRel: CmdArgRelationship) {
    cmdArgRelationships += argRel
  }
}
