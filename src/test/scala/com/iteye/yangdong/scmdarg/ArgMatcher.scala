package com.iteye.yangdong.scmdarg

/**
 * @author Winter Young
 */
case class ArgMatcher(argName: String) extends CmdArgMatcher {
  def matcherString = "--" + argName

  def simplifyMatcherTree() = this

  override def toString = argName

  def matches(valueTable: CmdArgValueTable) = valueTable.isValueGiven(argName)
}
