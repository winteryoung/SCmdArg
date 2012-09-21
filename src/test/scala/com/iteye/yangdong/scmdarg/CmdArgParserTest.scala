package com.iteye.yangdong.scmdarg

import org.scalatest.FunSuite
import CmdArgValueConverter._

/**
 * @author Winter Young
 */
class CmdArgParserTest extends FunSuite {
  test("Parse long name arg") {
    val args = Array("--arg1", "1")
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Int]("arg1")
    }
    cmdArgs.parse0()

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("Parse single short name arg") {
    val args = Array("-a", "1")
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Int]("arg1", shortName = Some('a'))
    }
    cmdArgs.parse0()

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("Parse compact short name arg") {
    val args = Array("-a1")
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Int]("arg1", shortName = Some('a'))
    }
    cmdArgs.parse0()

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("Parse present long name boolean arg") {
    val args = Array("--arg1")
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Boolean]("arg1")
    }
    cmdArgs.parse0()

    expect(true) {
      cmdArgs.arg1.get
    }
  }

  test("Parse absent long name boolean arg") {
    val args = Array[String]()
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Boolean]("arg1")
    }
    cmdArgs.parse0()

    expect(false) {
      cmdArgs.arg1.get
    }
  }

  test("Boolean CmdArg with a true default value") {
    intercept[Exception] {
      new CmdArgParser(Array[String]()) {
        val arg1 = arg[Boolean]("arg1", default = Some("true"))
      }
    }
  }

  test("Parse single short name boolean arg") {
    val args = Array("-a")
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Boolean]("arg1", shortName = Some('a'))
    }
    cmdArgs.parse0()

    expect(true) {
      cmdArgs.arg1.get
    }
  }

  test("Parse compact multiple short name boolean arg") {
    val args = Array("-ab")
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Boolean]("arg1", shortName = Some('a'))
      val arg2 = arg[Boolean]("arg2", shortName = Some('b'))
    }
    cmdArgs.parse0()

    expect(true) {
      cmdArgs.arg1.get
    }
    expect(true) {
      cmdArgs.arg2.get
    }
  }
  
  test("Have the required arg") {
    val args = Array("--arg1", "1")
    val cmdArgs = new CmdArgParser(args) {
      val arg1 = arg[Int]("arg1", isRequired = true)
    }
    cmdArgs.parse0()

    expect(1) {
      cmdArgs.arg1.get
    }
  }
  
  test("Miss the required arg") {
    intercept[CmdArgParserException] {
      val args = Array[String]()
      val cmdArgs = new CmdArgParser(args) {
        val arg1 = arg[Int]("arg1", isRequired = true)
      }
      cmdArgs.parse0()
    }
  }

  test("Invalid arg value for the arg type") {
    intercept[CmdArgParserException] {
      val args = Array("--arg1", "a")
      new CmdArgParser(args) {
        val arg1 = arg[Int]("arg1")
      }.parse0()
    }
  }

  test("depends on relationship") {
    intercept[CmdArgParserException] {
      val args = Array("--arg1", "1")
      new CmdArgParser(args) {
        val arg1 = arg[Int]("arg1")
        val arg2 = arg[Int]("arg2")

        rel(arg1 ~> arg2)
      }.parse0()
    }
  }

  test("Valid value set") {
    def createParser(args: Array[String]) = {
      new CmdArgParser(args) {
        val arg1 = arg[Int]("arg1", validValueSet = Some(Set("2", "3")))
      }
    }

    createParser(Array("--arg1", "2")).parse0()
    createParser(Array("--arg1", "3")).parse0()
    createParser(Array()).parse0()
    intercept[CmdArgParserException] {
      createParser(Array("--arg1", "1")).parse0()
    }
  }
}
