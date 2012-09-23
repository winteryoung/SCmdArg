package com.iteye.yangdong.scmdarg

import org.scalatest.FunSuite
import CmdArgValueConverter._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.StringWriter

/**
 * @author Winter Young
 */
@RunWith(classOf[JUnitRunner])
class CmdArgParserTest extends FunSuite {
  test("parse long name arg") {
    val args = Array("--arg1", "1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int]()
    }
    cmdArgs.parse0(args)

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("parse single short name arg") {
    val args = Array("-a", "1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int](shortName = Some('a'))
    }
    cmdArgs.parse0(args)

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("parse compact short name arg") {
    val args = Array("-a1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Int](shortName = Some('a'))
    }
    cmdArgs.parse0(args)

    expect(1) {
      cmdArgs.arg1.get
    }
  }

  test("parse present long name boolean arg") {
    val args = Array("--arg1")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Boolean]()
    }
    cmdArgs.parse0(args)

    expect(true) {
      cmdArgs.arg1.get
    }
  }

  test("parse absent long name boolean arg") {
    val args = Array[String]()
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Boolean]()
    }
    cmdArgs.parse0(args)

    expect(false) {
      cmdArgs.arg1.get
    }
  }

  test("boolean CmdArg with a true default value") {
    intercept[Exception] {
      new CmdArgParser("app") {
        val arg1 = arg[Boolean](default = Some("true"))
      }.parse0(Array())
    }
  }

  test("parse single short name boolean arg") {
    val args = Array("-a")
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = arg[Boolean](shortName = Some('a'))
    }
    cmdArgs.parse0(args)

    expect(true) {
      cmdArgs.arg1.get
    }
  }

  test("parse compact multiple short name boolean arg") {
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
  
  test("have the required arg") {
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

  test("invalid arg value for the arg type") {
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

  test("mutually depends on relationship") {
    intercept[CmdArgParserException] {
      val args = Array("--arg1", "1")
      new CmdArgParser("app") {
        val arg1 = arg[Int]()
        val arg2 = arg[Int]()

        rel(arg2 <~> arg1)
      }.parse0(args)
    }
  }

  test("valid value set") {
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

  test("default arg") {
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

  test("valid value set in multi-value arg") {
    val cmdArgs = new CmdArgParser("app") {
      val arg1 = marg[Int](validValueSet = Some(Set("1", "2", "3")))
    }
    cmdArgs.parse0(Array("--arg1", "1", "--arg1", "2"))
    intercept[CmdArgParserException] {
      cmdArgs.parse0(Array("--arg1", "4"))
    }
  }

  test("help") {
    val cmdArgs = new CmdArgParser("app", "test application") {
      val inputFile = arg[String](shortName = Some('i'),
        desc = "The input file.",
        isRequired = true,
        isDefault = true,
        valueName = "filePath")

      val verbose = arg[Int](shortName = Some('v'),
        desc = "The verbosity level of the output.",
        valueName = "n",
        default = Some("3"))

      val flag = arg[Boolean](shortName = Some('f'),
        desc = "Some flag")

      val action = arg[String](shortName = Some('a'),
        validValueSet = Some(Set("detectFileEncoding", "countLines")),
        desc = "The action of this app.",
        isRequired = true,
        valueName = "")
    }
    cmdArgs.parse0(Array("-i", "file", "-a", "countLines"))
    val sw = new StringWriter()
    cmdArgs.setOutWriter(sw)
    cmdArgs.displayHelp()

    val actualHelp = sw.toString
    val expectedHelp =
      """
        |app: test application
        |
        |Usage: app [OPTIONS]
        |
        |OPTIONS
        |
        |-h
        |--help
        |   Display this help message
        |
        |-i filePath
        |--inputFile filePath (required, default argument)
        |   The input file.
        |
        |-v n
        |--verbose n (default value: 3)
        |   The verbosity level of the output.
        |
        |-f
        |--flag
        |   Some flag
        |
        |-a
        |--action (required)
        |   The action of this app.
      """.stripMargin

    expect(expectedHelp.trim().split("\n").map(_.trim()).mkString("\n")) {
      actualHelp.trim().split("\n").map(_.trim()).mkString("\n")
    }
  }
}
