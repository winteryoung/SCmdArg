package com.iteye.yangdong.scmdarg

import org.scalatest.FunSuite
import CmdArgRelationshipTypeEnum._

/**
 * @author Winter Young
 */
class CmdArgRelationshipTest extends FunSuite {
  test("Normal depends on relationshiop") {
    val arg1 = ArgComb("arg1")
    val arg2 = ArgComb("arg2")
    val args = Set("arg1", "arg2")
    CmdArgRelationship(DependsOn, arg1, arg2).verify(args)
  }

  test("Unsuccessful depends on relationship") {
    val arg1 = ArgComb("arg1")
    val arg2 = ArgComb("arg2")
    val args = Set("arg1")
    intercept[CmdArgParserException] {
      CmdArgRelationship(DependsOn, arg1, arg2).verify(args)
    }
  }

  test("Normal exclusive from relationshiop") {
    val arg1 = ArgComb("arg1")
    val arg2 = ArgComb("arg2")
    val args = Set("arg2")
    CmdArgRelationship(ExclusiveFrom, arg1, arg2).verify(args)
  }

  test("Unsuccessful exclusive from relationship") {
    val arg1 = ArgComb("arg1")
    val arg2 = ArgComb("arg2")
    val args = Set("arg1", "arg2")
    intercept[CmdArgParserException] {
      CmdArgRelationship(ExclusiveFrom, arg1, arg2).verify(args)
    }
  }

  test("empty args") {
    val arg1 = ArgComb("arg1")
    val arg2 = ArgComb("arg2")
    val args = Set[String]()
    CmdArgRelationship(ExclusiveFrom, arg1, arg2).verify(args)
  }
}
