package com.banking.financial.services

import scala.collection.mutable.ListBuffer

object SimpleApp extends App {
  val oldGenStrings = new ListBuffer[String]
  def generateGarbage() = {
    var buffer = new ListBuffer()
    for(i ← 1 to 1000000) {
      buffer +: s"Some String"
    }

    oldGenStrings.appendAll(buffer.take(100000))
  }

  while(true) {
    generateGarbage()
    Thread.sleep {
      1000
    }
  }

}
