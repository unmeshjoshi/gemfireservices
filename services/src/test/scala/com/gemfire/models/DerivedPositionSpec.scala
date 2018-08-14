package com.gemfire.models

import org.scalatest.{FunSuite, Matchers}

class DerivedPositionSpec extends FunSuite with Matchers {

  test("should calculate value from balance and FX rate") {
    val position = new Position(1, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28")
    val fxRate = new FxRate("INR", "USD", new java.math.BigDecimal(2), "2018-01-28")

    val derivedPosition = new DerivedPosition(position, fxRate)

    assert(derivedPosition.value == 130134482 * 2)
  }
}
