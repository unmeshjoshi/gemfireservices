package com.gemfire.models

class DerivedPosition(val position: Position, fxRate: FxRate) {
  def value: Int = {
    position.getBalance * fxRate.getFxRate
  }
}
