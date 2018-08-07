package com.gemfire.models

case class AggregatedPosition(positions: Seq[DerivedPosition]) {
  def balance: BigDecimal = positions.map(p => BigDecimal(p.value)).sum
}
