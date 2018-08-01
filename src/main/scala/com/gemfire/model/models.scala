package com.gemfire.model

case class User(id: Int, name: String) {
  def this() = this(0, null)
}