package com.gemfire.models

class DerivedPosition(val position: Position, fxRate: FxRate) {
  def value: Int = {
    val v1: java.math.BigDecimal = position.getBalance
    val v2: java.math.BigDecimal = fxRate.getFxRate
    (v1.multiply(v2)).intValue()//FIXME
  }
}
