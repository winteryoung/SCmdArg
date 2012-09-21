package com.iteye.yangdong.scmdarg

import scala.util.control.ControlThrowable

/**
 * @author Winter Young
 */
class CmdArgParserException(msg: String) extends ControlThrowable {
  override def toString = msg
}
