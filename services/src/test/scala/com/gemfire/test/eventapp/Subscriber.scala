package com.gemfire.test.eventapp

import com.gemfire.eventhandlers.CustomEventHandler
import com.gemfire.models.Position
import com.gemfire.repository.ClientCacheProvider
import com.util.Networks
import org.apache.geode.cache.client.{ClientCacheFactory, ClientRegionShortcut}
import org.apache.geode.cache.{CacheListener, GemFireCache, Region}
import org.apache.geode.pdx.ReflectionBasedAutoSerializer

object Subscriber extends  App {
  val factory = new ClientCacheFactory()
  val clientCache = factory.addPoolLocator(new Networks().hostname(), 9009)
    .setPoolSubscriptionEnabled(true)
    .setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*"))
    .create()
  clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).addCacheListener(new CustomEventHandler).create("Positions")

  private val region: Region[AnyRef, Position] = clientCache.getRegion("Positions")
  region.registerInterest("ALL_KEYS")

  val myListener: CacheListener[AnyRef, Position] = region.getAttributes.getCacheListeners()(0)
  System.out.println("waiting for publisher to do " + 100 + " puts...")
  Thread.sleep(10000000)
}
