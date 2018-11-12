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
    val secProp = new Properties();
    secProp.put("security-username","geode")
    secProp.put("security-password", "geodePass")
    secProp.put("security-client-auth-init", "com.gemfire.authorization.GemfireAuthenticator.create")

    val factory = new ClientCacheFactory(secProp)
//    val clientCache = factory.addPoolLocator(new Networks().hostname(), 9009)
    val clientCache = factory.addPoolLocator("127.0.0.1", 9009)
      .setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*"))
      .create()
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("Positions")
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("FxRates")
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("MarketPrices")
    clientCache
  }
}
