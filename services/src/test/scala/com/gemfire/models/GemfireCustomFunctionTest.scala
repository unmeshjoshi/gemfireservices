package com.gemfire.models

import com.gemfire.repository.ClientCacheProvider
import org.scalatest.FunSuite

class GemfireCustomFunctionTest extends FunSuite {

  val clientCacheProvider = new ClientCacheProvider()

  test("should call custom function with gemfire") {
    val result = new JPositionCache(clientCacheProvider.clientCache).multiplyOnServer(10, 2)
    assert(result == 20)
  }
}
