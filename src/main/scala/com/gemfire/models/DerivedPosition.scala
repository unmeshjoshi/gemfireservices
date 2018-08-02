package com.gemfire.models

class DerivedPosition(position: Position, fxRate: FxRate) {
  def value: Int = {
    position.getBalance * fxRate.getFxRate
  }
}
