package com.gemfire.repository

import com.util.Networks
import org.apache.geode.cache.client.{ClientCache, ClientCacheFactory, ClientRegionShortcut}
import org.apache.geode.pdx.ReflectionBasedAutoSerializer

class ClientCacheProvider {
  val clientCache: ClientCache = createClientCache()

  private def createClientCache() = {
    val factory = new ClientCacheFactory()
    val clientCache = factory.addPoolLocator(new Networks().hostname(), 9009)
      .setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*"))
      .create()
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("Positions")
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("FxRates")
    clientCache
  }
}
