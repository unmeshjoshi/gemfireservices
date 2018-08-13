package com.gemfire.models

class DerivedPosition(val position: Position, fxRate: FxRate) {
  def value: Int = {
    val v1 = position.getBalance
    val v2: Double = fxRate.getFxRate
    (v1 * v2).toInt //FIXME
  }
}
