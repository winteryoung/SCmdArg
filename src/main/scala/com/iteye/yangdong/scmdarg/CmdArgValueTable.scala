package com.iteye.yangdong.scmdarg

import collection.mutable
import util.MultiMap

/**
 * @author Winter Young
 */
class CmdArgValueTable(values: (String, String)*) {
  private val table = new mutable.HashMap[String, mutable.ArrayBuffer[String]] with MultiMap[String, String]

  {
    for ((k, v) <- values) {
      table.addBinding(k, v)
    }
  }

  def isValueGiven(argName: String) = {
    table.contains(argName)
  }

  def argNames = table.keySet

  def apply(argName: String): Seq[String] = {
    table(argName)
  }

  def addValue(argName: String, argValue: String) {
    table.addBinding(argName, argValue)
  }
}
