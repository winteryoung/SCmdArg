package com.iteye.yangdong.scmdarg

/**
 * @author Winter Young
 */
sealed abstract class CmdArg[T](val cmdArgTable: CmdArgTable,
                                private var _argName: String,
                                val desc: String = "",
                                val shortName: Option[Char] = None,
                                val valueName: String = "",
                                val isRequired: Boolean = false,
                                val isDefault: Boolean = false,
                                val default: Option[String] = None,
                                val validValueSet: Option[Set[String]] = None)
  extends CmdArgMatcher {

  def getOption: Option[T]
  val isBooleanCmdArg: Boolean
  def validateValue(argValue: String): Boolean

  protected def onLoaded() {
    if (argName.length < 2) {
      throw new Exception("Argument name can't be less than 2 characters.")
    }
    if (isBooleanCmdArg) {
      if (!valueName.isEmpty) {
        throw new Exception("Boolean argument can't have value name.")
      }
      default match {
        case Some(d) => throw new Exception("Boolean argument can't have a true default value")
        case _ =>
      }
    }
  }

  def get = {
    getOption match {
      case Some(v) => v
      case _ => throw new Exception("Value of --%s not given" format argName)
    }
  }

  def hasValue: Boolean = getOption.isDefined

  def matcherString = "--" + argName

  def simplifyMatcherTree() = this

  def argName = _argName

  private[scmdarg] def argName_=(v: String) {
    if (v.length < 2) {
      throw new Exception("Argument name can't be less than 2 characters.")
    }
    _argName = v
  }

  def matches(valueTable: CmdArgValueTable) = valueTable.isValueGiven(argName)
}

case class SingleValueCmdArg[T](override val cmdArgTable: CmdArgTable,
                                private var _argName: String,
                                override val desc: String = "",
                                override val shortName: Option[Char] = None,
                                override val valueName: String = "",
                                override val isRequired: Boolean = false,
                                override val isDefault: Boolean = false,
                                override val default: Option[String] = None,
                                override val validValueSet: Option[Set[String]] = None)
                               (implicit valueConverter: CmdArgValueConverter[T],
                                classManifestOfT: ClassManifest[T])
  extends CmdArg[T](cmdArgTable, _argName, desc, shortName, valueName, isRequired, isDefault, default, validValueSet) {

  private var _isBooleanCmdArg: Boolean = false

  {
    _isBooleanCmdArg = classOf[Boolean].isAssignableFrom(classManifest[T].erasure)
  }

  val isBooleanCmdArg = _isBooleanCmdArg

  def getOption = {
    if (cmdArgTable.isValueGiven(argName)) {
      Some(cmdArgTable.getValue(argName)(0))
    }
    else {
      None
    }
  }

  def validateValue(argValue: String) = {
    try {
      valueConverter(argValue)
      true
    }
    catch {
      case _: Exception => false
    }
  }

  def ===(expectedValue: String): CmdArgMatcher = EqualityCmdArgMatcher(argName, expectedValue, _ == _)
  def !==(expectedValue: String) = EqualityCmdArgMatcher(argName, expectedValue, _ != _)

  onLoaded()
}

case class MultiValueCmdArg[T](override val cmdArgTable: CmdArgTable,
                               private var _argName: String,
                               override val desc: String = "",
                               override val shortName: Option[Char] = None,
                               override val valueName: String = "",
                               override val isRequired: Boolean = false,
                               override val isDefault: Boolean = false,
                               override val default: Option[String] = None,
                               override val validValueSet: Option[Set[String]] = None)
                              (implicit valueConverter: CmdArgValueConverter[T])
  extends CmdArg[Seq[T]](cmdArgTable, _argName, desc, shortName, valueName, isRequired, isDefault, default, validValueSet) {

  def getOption = {
    if (cmdArgTable.isValueGiven(argName)) {
      Some(Seq(cmdArgTable.getValue(argName).map(valueConverter(_)): _*))
    }
    else {
      None
    }
  }

  val isBooleanCmdArg = false

  def validateValue(argValue: String) = {
    try {
      valueConverter(argValue)
      true
    }
    catch {
      case _: Exception => false
    }
  }

  def contains(expectedValue: String): CmdArgMatcher = ContainmentCmdArgMatcher(argName, expectedValue, _ == _)
  def notContains(expectedValue: String): CmdArgMatcher = ContainmentCmdArgMatcher(argName, expectedValue, _ != _)

  onLoaded()
}