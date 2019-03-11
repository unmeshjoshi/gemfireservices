package com.gemfire.test

import java.util.regex.Pattern

import org.scalatest.{FunSuite, TestSuite}

class SelectExpressionMatchingTest extends FunSuite {
  private val SELECT_EXPR = "\\s*SELECT\\s+.+\\s+FROM.+"
  private val SELECT_EXPR_PATTERN = Pattern.compile(SELECT_EXPR, Pattern.CASE_INSENSITIVE)

  test("should match select expr") {
    assert(SELECT_EXPR_PATTERN .matcher("select sum(t.size()) from /Positions where t.get(0).ppCd IN SET('1')").matches())
    assert(SELECT_EXPR_PATTERN .matcher("select t.get(0) from /Positions").matches())
  }
}
