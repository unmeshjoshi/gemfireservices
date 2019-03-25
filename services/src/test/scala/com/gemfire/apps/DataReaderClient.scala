package com.gemfire.apps

import com.gemfire.repository.{ClientCacheProvider, PositionCache, TransactionCache}
object DataReaderClient extends App {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  val transactionCache = new TransactionCache(ClientCacheProvider.clientCache)


  private val value = transactionCache.executeOql("""select ent.value from /Transactions.entrySet ent where ent.value.transactionId in
                                                    set(
                                                    '99523821610_2018-1-2',
                                                    '9952384916_2018-1-2' ,
                                                    '99523853431_2018-1-2',
                                                    '99523839567_2018-1-2',
                                                    '99523826975_2018-1-2',
                                                    '99523823729_2018-1-2',
                                                    '99523830023_2018-1-2',
                                                    '99523836355_2018-1-2',
                                                    '99523831419_2018-1-2',
                                                    '99523859563_2018-1-2')""")
  println(value)


  private val region = positionCache.positionRegion

  private val oldPosition = positionCache.get("28397") // 30_2018-01-28_453398853

  positionCache.get("28398") // 30_2018-01-28_453398853

  positionCache.get("28400") // 30_2018-01-28_453398853
  println(oldPosition)
}
