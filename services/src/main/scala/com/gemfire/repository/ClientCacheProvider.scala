package com.gemfire.repository

import java.util.Properties

import com.util.Networks
import org.apache.geode.cache.{Cache, GemFireCache}
import org.apache.geode.cache.client.{ClientCache, ClientCacheFactory, ClientRegionShortcut}
import org.apache.geode.pdx.ReflectionBasedAutoSerializer

object ClientCacheProvider {

  import org.apache.geode.LogWriter
  import org.apache.geode.distributed.DistributedMember
  import org.apache.geode.security.AuthInitialize
  import org.apache.geode.security.AuthenticationFailedException

  val clientCache: GemFireCache = createClientCache()

  private def createClientCache():GemFireCache = {
    val factory = new ClientCacheFactory()
    val clientCache = factory.addPoolLocator("172.17.0.2", 9009)
      .setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*"))
      .setPoolMinConnections(50)

//      .setPoolMaxConnections(-1) //unlimited
//      .setPoolPRSingleHopEnabled(true)

     .create()
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("Positions")
//    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("FxRates")
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("MarketPrices")
    clientCache
  }
}
