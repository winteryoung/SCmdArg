package com.iteye.yangdong.scmdarg

/**
 * @author Winter Young
 */
case class CmdArg[T](cmdArgTable: CmdArgTable,
                     argName: String,
                     desc: String = "",
                     shortName: Option[Char] = None,
                     valueName: String = "",
                     isRequired: Boolean = false,
                     isDefault: Boolean = false,
                     default: Option[String] = None,
                     validValueSet: Option[Set[String]] = None)
                    (implicit val valueConverter: CmdArgValueConverter[T],
                              classManifestOfT: ClassManifest[T])
  extends CmdArgCombination {

  def get: T = {
    if (cmdArgTable.isValueGiven(argName)) {
      cmdArgTable.getValue(argName)
    }
    else {
      throw new Exception("Missing option: " + argName)
    }
  }

  def hasValue = {
    cmdArgTable.isValueGiven(argName)
  }

  def getOption: Option[T] = {
    if (cmdArgTable.isValueGiven(argName)) {
      Some(cmdArgTable.getValue(argName))
    }
    else {
      None
    }
  }

  private[scmdarg] def isBooleanCmdArg: Boolean = {
    if (classOf[Boolean].isAssignableFrom(classManifest[T].erasure)) {
      true
    }
    else {
      false
    }
  }

  def matches(argNames: collection.Set[String]) = {
    argNames(argName)
  }

  def cmdArgCombinationStr = "--" + argName

  def simplify() = this
}