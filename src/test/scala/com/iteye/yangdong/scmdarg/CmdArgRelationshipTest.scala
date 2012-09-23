package com.iteye.yangdong.scmdarg

import org.scalatest.FunSuite
import CmdArgRelationshipTypeEnum._

/**
 * @author Winter Young
 */
class CmdArgRelationshipTest extends FunSuite {
  test("Normal depends on relationshiop") {
    val arg1 = ArgMatcher("arg1")
    val arg2 = ArgMatcher("arg2")
    val valueTable = new CmdArgValueTable(("arg1", "1"), ("arg2", "1"))
    CmdArgRelationship(DependsOn, arg1, arg2).verify(valueTable)
  }

  test("Unsuccessful depends on relationship") {
    val arg1 = ArgMatcher("arg1")
    val arg2 = ArgMatcher("arg2")
    val valueTable = new CmdArgValueTable(("arg1", "1"))
    intercept[CmdArgParserException] {
      CmdArgRelationship(DependsOn, arg1, arg2).verify(valueTable)
    }
  }

  test("Normal exclusive from relationshiop") {
    val arg1 = ArgMatcher("arg1")
    val arg2 = ArgMatcher("arg2")
    val valueTable = new CmdArgValueTable(("arg2", "1"))
    CmdArgRelationship(ExclusiveFrom, arg1, arg2).verify(valueTable)
  }

  test("Unsuccessful exclusive from relationship") {
    val arg1 = ArgMatcher("arg1")
    val arg2 = ArgMatcher("arg2")
    val valueTable = new CmdArgValueTable(("arg1", "1"), ("arg2", "1"))
    intercept[CmdArgParserException] {
      CmdArgRelationship(ExclusiveFrom, arg1, arg2).verify(valueTable)
    }
  }

  test("empty args") {
    val arg1 = ArgMatcher("arg1")
    val arg2 = ArgMatcher("arg2")
    val valueTable = new CmdArgValueTable()
    CmdArgRelationship(ExclusiveFrom, arg1, arg2).verify(valueTable)
  }
}
