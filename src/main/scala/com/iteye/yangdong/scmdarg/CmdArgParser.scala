package com.iteye.yangdong.scmdarg

import java.io.{PrintWriter, Writer}
import collection.mutable

/**
 * @author Winter Young
 */
abstract class CmdArgParser(realArgs: Array[String]) {
  private val cmdArgTable = new CmdArgTable()
  private val cmdArgRelationships = mutable.ListBuffer[CmdArgRelationship]()

  private var autoExit = true

  private var error = false

  private var _outWriter: Writer = new PrintWriter(System.out)
  private var _errWriter: Writer = new PrintWriter(System.err)

  private var _appDesc: String = ""

  protected val helpArg = arg[Boolean]("help", desc = "Display this help message", shortName = Some('h'))

  private val newline = System.getProperty("line.separator")

  def outWriter_=(writer: Writer) {
    _outWriter = writer
  }

  def errWriter_=(writer: Writer) {
    _errWriter = writer
  }

  def setAutoExit(autoExit: Boolean) {
    this.autoExit = autoExit
  }

  def hasError: Boolean = error

  def parse() {
    try {
      parse0()

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

  private[scmdarg] def parse0() {
    populateCmdArgTable()
    validate()
  }

  private def displayArgsDescription() {
    for (argName <- cmdArgTable.argNameSeq) {
      _outWriter.write("   --" + argName)
      val cmdArg = cmdArgTable.getDef(argName)
      if (cmdArg.shortName != None) {
        _outWriter.write(", -" + cmdArg.shortName.get)
      }
      _outWriter.write(":" + newline)
      _outWriter.write("      " + cmdArg.desc + newline)
      if (cmdArg.isRequired) {
        _outWriter.write("      Is required" + newline)
      }
      if (cmdArg.isDefault) {
        _outWriter.write("      Is default" + newline)
      }
      if (cmdArg.default != None) {
        _outWriter.write("      Default value = " + cmdArg.default.get + newline)
      }
    }
  }

  def displayHelp() {
    _outWriter.write(_appDesc + newline)
    displayArgsDescription()
    _outWriter.flush()
  }

  private def validate() {
    cmdArgTable.validateRequiredness()

    for (rel <- cmdArgRelationships) {
      rel.verify(cmdArgTable.argNameSet)
    }
  }

  private def populateCmdArgTable() {
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
        val defaultArgName = cmdArgTable.getDefaultArgName
        cmdArgTable.setArgValue(defaultArgName, realArg)
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

  def arg[T](argName: String,
             desc: String = "",
             shortName: Option[Char] = None,
             isRequired: Boolean = false,
             isDefault: Boolean = false,
             default: Option[String] = None,
             validValueSet: Option[Set[String]] = None)
            (implicit valueConverter: CmdArgValueConverter[T],
                      classManifestOfT: ClassManifest[T]): CmdArg[T] = {
    val cmdArg = CmdArg(cmdArgTable, argName, desc, shortName, isRequired, isDefault, default, validValueSet = validValueSet)
    cmdArgTable.defineArg(cmdArg)
    cmdArg
  }

  def rel(argRel: CmdArgRelationship) {
    cmdArgRelationships += argRel
  }

  def appDesc: String = _appDesc

  def appDesc_=(appDescription: String) {
    _appDesc = appDescription
  }
}
