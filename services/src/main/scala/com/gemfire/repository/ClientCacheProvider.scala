package com.gemfire.repository

import java.net.InetSocketAddress

import org.apache.geode.cache.client.internal.PoolImpl
import org.apache.geode.cache.client.{ClientCache, ClientCacheFactory, ClientRegionShortcut, PoolManager}
import org.apache.geode.cache.{GemFireCache, Region}
import org.apache.geode.pdx.ReflectionBasedAutoSerializer

import scala.collection.mutable

object ClientCacheProvider {

  lazy val clientCache: GemFireCache = createClientCache("locator2")

  private def createClientCache(locatorIp: String):GemFireCache = {
    val factory = new ClientCacheFactory()
    val clientCache = factory.addPoolLocator(locatorIp, 9009)
      .setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*,com.gemfire.functions.*"))
      .setPoolMinConnections(50)
      .setPdxReadSerialized(true)
      .setPoolReadTimeout(60000)
     .create()
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("Positions")
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("FxRates")
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("MarketPrices")
    clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("Transactions")
    clientCache
  }

  def switchCluster() = createClientCache("10.131.21.69")

  def healthCheckCluster() = {

    import org.apache.geode.cache.client.internal.PoolImpl

    import scala.collection.JavaConverters._

    val pool = clientCache.asInstanceOf[ClientCache].getDefaultPool().asInstanceOf[PoolImpl]
    val source = pool.getConnectionSource
    val locators: Seq[InetSocketAddress] = source.getOnlineLocators.asScala.toSeq
    val servers = source.getAllServers.asScala.toSeq

    if (!atLeastOneLocatorIsUp(locators) || !allServersAreUp(servers))
        switchCluster()
  }

  def allServersAreUp(servers:Seq[Any]):Boolean = servers.size == 3
  def atLeastOneLocatorIsUp(locators:Seq[Any]):Boolean = locators.size >= 1

  private def closeAndCreateCobCache = {
    clientCache.close()
  }

  private def getPool(r: Region[Any, Any])
  {
    val poolName = r.getAttributes.getPoolName
    PoolManager.find(poolName).asInstanceOf[PoolImpl]
  }
}
