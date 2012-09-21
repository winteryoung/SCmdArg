package com.iteye.yangdong.scmdarg

import java.nio.file._
import java.net.URL

/**
 * @author Winter Young
 */
abstract class CmdArgValueConverter[T: ClassManifest] extends (String => T) {
  def convert(v1: String): T

  def customErrorMsg(v1: String): Option[String] = None

  def apply(v1: String) = {
    try {
      convert(v1)
    }
    catch {
      case e: Exception => {
        customErrorMsg(v1) match {
          case Some(custMsg) =>
            throw new CmdArgParserException(custMsg)
          case _ => {
            val className = classManifest[T].erasure.getSimpleName
            throw new CmdArgParserException(""""%s" is not a %s""" format (v1, className))
          }
        }
      }
    }
  }
}

object CmdArgValueConverter {
  implicit object String2String extends CmdArgValueConverter[String] {
    def convert(str: String) = str
  }

  implicit object String2Byte extends CmdArgValueConverter[Byte] {
    def convert(v1: String) = java.lang.Byte.parseByte(v1)
  }

  implicit object String2Short extends CmdArgValueConverter[Short] {
    def convert(v1: String) = java.lang.Short.parseShort(v1)
  }

  implicit object String2Int extends CmdArgValueConverter[Int] {
    def convert(str: String) = java.lang.Integer.parseInt(str)
  }

  implicit object String2Long extends CmdArgValueConverter[Long] {
    def convert(v1: String) = java.lang.Long.parseLong(v1)
  }

  implicit object String2Float extends CmdArgValueConverter[Float] {
    def convert(v1: String) = java.lang.Float.parseFloat(v1)
  }

  implicit object String2Double extends CmdArgValueConverter[Double] {
    def convert(str: String): Double = java.lang.Double.parseDouble(str)
  }

  implicit object String2Boolean extends CmdArgValueConverter[Boolean] {
    def convert(str: String) = java.lang.Boolean.parseBoolean(str)
  }

  implicit object String2Path extends CmdArgValueConverter[Path] {
    def convert(str: String) = Paths.get(str)
  }

  implicit object String2PathMatcher extends CmdArgValueConverter[PathMatcher] {
    def convert(str: String) = FileSystems.getDefault.getPathMatcher(str)

    override def customErrorMsg(v1: String) = Some("\"%s\" is not a path matcher" format v1)
  }

  implicit object String2Url extends CmdArgValueConverter[URL] {
    def convert(v1: String) = new URL(v1)
  }
}