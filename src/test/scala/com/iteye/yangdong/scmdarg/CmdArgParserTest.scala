package com.iteye.yangdong.scmdarg

import org.scalatest.FunSuite
import CmdArgValueConverter._

/**
 * @author Winter Young
 */
class CmdArgParserTest extends FunSuite {
  test("Parse long name arg") {
    val args = Array("--arg1", "1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int]()
    }
    cmdArgs.parse0(args)

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("Parse single short name arg") {
    val args = Array("-a", "1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int](shortName = Some('a'))
    }
    cmdArgs.parse0(args)

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("Parse compact short name arg") {
    val args = Array("-a1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int](shortName = Some('a'))
    }
    cmdArgs.parse0(args)

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("Parse present long name boolean arg") {
    val args = Array("--arg1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Boolean]()
    }
    cmdArgs.parse0(args)

    expect(true) {
      cmdArgs.arg1.get
    }
  }

  test("Parse absent long name boolean arg") {
    val args = Array[String]()
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Boolean]()
    }
    cmdArgs.parse0(args)

    expect(false) {
      cmdArgs.arg1.get
    }
  }

  test("Boolean CmdArg with a true default value") {
    intercept[Exception] {
      new CmdArgParser("app") {
        val arg1 = arg[Boolean](default = Some("true"))
      }.parse0(Array())
    }
  }

  test("Parse single short name boolean arg") {
    val args = Array("-a")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Boolean](shortName = Some('a'))
    }
    cmdArgs.parse0(args)

    expect(true) {
      cmdArgs.arg1.get
    }
  }

  test("Parse compact multiple short name boolean arg") {
    val args = Array("-ab")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Boolean](shortName = Some('a'))
      val arg2 = arg[Boolean](shortName = Some('b'))
    }
    cmdArgs.parse0(args)

    expect(true) {
      cmdArgs.arg1.get
    }
    expect(true) {
      cmdArgs.arg2.get
    }
  }
  
  test("Have the required arg") {
    val args = Array("--arg1", "1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int](isRequired = true)
    }
    cmdArgs.parse0(args)

    expect(1) {
      cmdArgs.arg1.get
    }
  }
  
  test("Miss the required arg") {
    intercept[CmdArgParserException] {
      val args = Array[String]()
      val cmdArgs = new CmdArgParser("app") {
        val arg1 = arg[Int](isRequired = true)
      }
      cmdArgs.parse0(args)
    }
  }

  test("Invalid arg value for the arg type") {
    intercept[CmdArgParserException] {
      val args = Array("--arg1", "a")
      new CmdArgParser("app") {
        val arg1 = arg[Int]()
      }.parse0(args)
    }
  }

  test("depends on relationship") {
    intercept[CmdArgParserException] {
      val args = Array("--arg1", "1")
      new CmdArgParser("app") {
        val arg1 = arg[Int]()
        val arg2 = arg[Int]()

        rel(arg1 ~> arg2)
      }.parse0(args)
    }
  }

  test("Valid value set") {
    def createParser() = {
      new CmdArgParser("app") {
        val arg1 = arg[Int](validValueSet = Some(Set("2", "3")))
      }
    }

    createParser().parse0(Array("--arg1", "2"))
    createParser().parse0(Array("--arg1", "3"))
    createParser().parse0(Array())
    intercept[CmdArgParserException] {
      createParser().parse0(Array("--arg1", "1"))
    }
  }

  test("Default arg") {
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int](isDefault = true)
    }
    cmdArgs.parse0(Array("1"))
    expect(1) { cmdArgs.arg1.get }
  }

  test("multi-value arg") {
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = marg[Int]()
    }
    cmdArgs.parse0(Array("--arg1", "1", "--arg1", "2", "--arg1", "3"))
    expect(Seq(1, 2, 3)) { cmdArgs.arg1.get }
  }
}
