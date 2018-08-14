package com.gemfire.models

import com.gemfire.repository.{ClientCacheProvider, JPositionCache}
import org.scalatest.FunSuite

class GemfireCustomFunctionTest extends FunSuite {

  test("should call custom function with gemfire") {
    val result = new JPositionCache(ClientCacheProvider.clientCache).multiplyOnServer(10, 2)
    assert(result == 20)
  }
}
