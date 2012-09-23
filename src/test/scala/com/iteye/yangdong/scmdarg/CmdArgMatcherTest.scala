package com.iteye.yangdong.scmdarg

import org.scalatest.FunSuite
import AndOrEnum._

/**
 * @author Winter Young
 */
class CmdArgMatcherTest extends FunSuite {
  def argMatcher(argName: String) = {
    ArgMatcher(argName)
  }

  test("'And' matches successfully") {
    val valueTable = new CmdArgValueTable
    valueTable.addValue("arg1", "1")
    valueTable.addValue("arg2", "1")
    val arg1 = argMatcher("arg1")
    val arg2 = argMatcher("arg2")
    val matcher = arg1 and arg2

    expect(true) {
      matcher.matches(valueTable)
    }
  }

  test("'And' matches unsuccessfully") {
    val arg1 = argMatcher("arg1")
    val arg2 = argMatcher("arg2")
    val matcher = arg1 and arg2

    expect(false) {
      val valueTable = new CmdArgValueTable
      valueTable.addValue("arg1", "1")
      matcher.matches(valueTable)
    }
    expect(false) {
      val valueTable = new CmdArgValueTable
      valueTable.addValue("arg2", "1")
      matcher.matches(valueTable)
    }
  }

  test("'Or' matches successfully") {
    val arg1 = argMatcher("arg1")
    val arg2 = argMatcher("arg2")
    val matcher = arg1 or arg2

    expect(true) {
      val valueTable = new CmdArgValueTable
      valueTable.addValue("arg1", "1")
      matcher.matches(valueTable)
    }
    expect(true) {
      val valueTable = new CmdArgValueTable
      valueTable.addValue("arg2", "1")
      matcher.matches(valueTable)
    }
  }

  test("'Or' matches unsuccessfully") {
    val arg1 = argMatcher("arg1")
    val arg2 = argMatcher("arg2")
    val matcher = arg1 or arg2

    expect(false) {
      matcher.matches(new CmdArgValueTable)
    }
  }

  test("Simplify") {
    val arg1 = argMatcher("arg1")
    val arg2 = argMatcher("arg2")
    val arg3 = argMatcher("arg3")
    val matcher = arg1 and arg2 and arg3

    matcher.simplifyMatcherTree() match {
      case AndOrCmdArgMatcher(op, ops) => {
        expect(And)(op)
        expect(Seq(arg1, arg2, arg3))(ops)
      }
    }
  }

  test("matcherString") {
    val arg1 = argMatcher("arg1")
    val arg2 = argMatcher("arg2")
    val arg3 = argMatcher("arg3")
    val arg4 = argMatcher("arg4")
    val matcher = arg1 and arg2 and arg3 or arg4

    expect("((--arg1 and --arg2 and --arg3) or --arg4)")(matcher.matcherString)
  }

  test("equality matcher") {
    expect(true) {
      val valueTable = new CmdArgValueTable(("arg1", "1"))
      EqualityCmdArgMatcher("arg1", "1").matches(valueTable)
    }
    expect(false) {
      val valueTable = new CmdArgValueTable(("arg1", "2"))
      EqualityCmdArgMatcher("arg1", "1").matches(valueTable)
    }
  }

  test("containment matcher") {
    val valueTable = new CmdArgValueTable(("arg1", "1"), ("arg1", "2"))
    expect(true) {
      ContainmentCmdArgMatcher("arg1", "1").matches(valueTable)
      ContainmentCmdArgMatcher("arg1", "2").matches(valueTable)
    }
    expect(false) {
      ContainmentCmdArgMatcher("arg1", "3").matches(valueTable)
    }
  }
}
