package com.iteye.yangdong.scmdarg

/**
 * @author Winter Young
 */
case class ArgComb(argName: String) extends CmdArgCombination {
  def matches(argNames: collection.Set[String]) = argNames(argName)

  def cmdArgCombinationStr = "--" + argName

  def simplify() = this

  override def toString = argName
}
