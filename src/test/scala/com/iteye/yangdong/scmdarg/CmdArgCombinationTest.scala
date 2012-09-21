package com.iteye.yangdong.scmdarg

import org.scalatest.FunSuite
import AndOrEnum._

/**
 * @author Winter Young
 */
class CmdArgCombinationTest extends FunSuite {
  def argComb(argName: String) = {
    ArgComb(argName)
  }

  test("'And' matches successfully") {
    val argNames = Set("arg1", "arg2")
    val arg1 = argComb("arg1")
    val arg2 = argComb("arg2")
    val comb = arg1 and arg2

    expect(true) {
      comb.matches(argNames)
    }
  }

  test("'And' matches unsuccessfully") {
    val arg1 = argComb("arg1")
    val arg2 = argComb("arg2")
    val comb = arg1 and arg2

    expect(false) {
      comb.matches(Set("arg1"))
      comb.matches(Set("arg2"))
    }
  }

  test("'Or' matches successfully") {
    val arg1 = argComb("arg1")
    val arg2 = argComb("arg2")
    val comb = arg1 or arg2

    expect(true) {
      comb.matches(Set("arg1"))
      comb.matches(Set("arg2"))
    }
  }

  test("'Or' matches unsuccessfully") {
    val argNames = Set[String]()
    val arg1 = argComb("arg1")
    val arg2 = argComb("arg2")
    val comb = arg1 or arg2

    expect(false) {
      comb.matches(argNames)
    }
  }

  test("Simplify") {
    val arg1 = argComb("arg1")
    val arg2 = argComb("arg2")
    val arg3 = argComb("arg3")
    val comb = arg1 and arg2 and arg3

    comb.simplify() match {
      case StdCmdArgCombination(op, ops) => {
        expect(And)(op)
        expect(Seq(arg1, arg2, arg3))(ops)
      }
    }
  }

  test("cmdArgCombinationStr") {
    val arg1 = argComb("arg1")
    val arg2 = argComb("arg2")
    val arg3 = argComb("arg3")
    val arg4 = argComb("arg4")
    val comb = arg1 and arg2 and arg3 or arg4

    expect("((--arg1 and --arg2 and --arg3) or --arg4)")(comb.cmdArgCombinationStr)
  }
}
