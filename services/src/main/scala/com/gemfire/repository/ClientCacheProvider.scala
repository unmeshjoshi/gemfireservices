package com.gemfire.repository

import java.io.InputStream
import java.net.InetSocketAddress
import java.util
import java.util.Properties
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import com.gemfire.repository.ClientCacheProvider.ClusterState
import javax.naming.Context
import org.apache.geode.{CancelCriterion, LogWriter}
import org.apache.geode.cache.client.internal.PoolImpl
import org.apache.geode.cache.client.{ClientCache, ClientCacheFactory, ClientRegionShortcut, PoolManager}
import org.apache.geode.cache.control.ResourceManager
import org.apache.geode.cache.query.QueryService
import org.apache.geode.cache.wan.GatewaySenderFactory
import org.apache.geode.cache.{CacheTransactionManager, Declarable, DiskStore, DiskStoreFactory, GemFireCache, Region, RegionAttributes}
import org.apache.geode.distributed.DistributedSystem
import org.apache.geode.pdx.{PdxInstance, PdxInstanceFactory, PdxSerializer, ReflectionBasedAutoSerializer}

class GemfireCacheDecorator(clusterState:AtomicReference[ClusterState]) extends GemFireCache {

  override def getName: String = clusterState.get().clientCacheInstance.getName

  override def getDistributedSystem: DistributedSystem = clusterState.get().clientCacheInstance.getDistributedSystem

  override def getResourceManager: ResourceManager = clusterState.get().clientCacheInstance.getResourceManager

  override def setCopyOnRead(copyOnRead: Boolean): Unit = clusterState.get().clientCacheInstance.setCopyOnRead(copyOnRead)

  override def getCopyOnRead: Boolean = clusterState.get().clientCacheInstance.getCopyOnRead

  override def getRegionAttributes[K, V](id: String): RegionAttributes[K, V] = clusterState.get().clientCacheInstance.getRegionAttributes(id)

  override def setRegionAttributes[K, V](id: String, attrs: RegionAttributes[K, V]): Unit = clusterState.get().clientCacheInstance.setRegionAttributes(id, attrs)

  override def listRegionAttributes[K, V](): util.Map[String, RegionAttributes[K, V]] = clusterState.get().clientCacheInstance.listRegionAttributes()

  override def loadCacheXml(is: InputStream): Unit = clusterState.get().clientCacheInstance.loadCacheXml(is)

  override def getLogger: LogWriter = clusterState.get().clientCacheInstance.getLogger

  override def getSecurityLogger: LogWriter = clusterState.get().clientCacheInstance.getSecurityLogger

  override def findDiskStore(name: String): DiskStore = clusterState.get().clientCacheInstance.findDiskStore(name)

  override def createDiskStoreFactory(): DiskStoreFactory = clusterState.get().clientCacheInstance.createDiskStoreFactory()

  override def createGatewaySenderFactory(): GatewaySenderFactory = clusterState.get().clientCacheInstance.createGatewaySenderFactory()

  override def getPdxReadSerialized: Boolean = clusterState.get().clientCacheInstance.getPdxReadSerialized

  override def getPdxSerializer: PdxSerializer = clusterState.get().clientCacheInstance.getPdxSerializer

  override def getPdxDiskStore: String = clusterState.get().clientCacheInstance.getPdxDiskStore

  override def getPdxPersistent: Boolean = clusterState.get().clientCacheInstance.getPdxPersistent

  override def getPdxIgnoreUnreadFields: Boolean = clusterState.get().clientCacheInstance.getPdxIgnoreUnreadFields

  override def getCacheTransactionManager: CacheTransactionManager = clusterState.get().clientCacheInstance.getCacheTransactionManager

  override def getJNDIContext: Context = clusterState.get().clientCacheInstance.getJNDIContext

  override def getInitializer: Declarable = clusterState.get().clientCacheInstance.getInitializer

  override def getInitializerProps: Properties = clusterState.get().clientCacheInstance.getInitializerProps

  override def getCancelCriterion: CancelCriterion = clusterState.get().clientCacheInstance.getCancelCriterion

  override def getRegion[K, V](path: String): Region[K, V] = clusterState.get().clientCacheInstance.getRegion(path)

  override def rootRegions(): util.Set[Region[_, _]] = clusterState.get().clientCacheInstance.rootRegions()

  override def createPdxInstanceFactory(className: String): PdxInstanceFactory = clusterState.get().clientCacheInstance.createPdxInstanceFactory(className)

  override def createPdxEnum(className: String, enumName: String, enumOrdinal: Int): PdxInstance = clusterState.get().clientCacheInstance.createPdxEnum(className, enumName, enumOrdinal)

  override def getQueryService: QueryService = clusterState.get().clientCacheInstance.getQueryService

  override def close(): Unit = clusterState.get().clientCacheInstance.close()

  override def isClosed: Boolean = clusterState.get().clientCacheInstance.isClosed
}

object ClientCacheProvider {
  private var instance: AtomicReference[ClusterState] = _
  private val scheduler: ScheduledExecutorService =
    Executors.newScheduledThreadPool(1)

  def create = {
    if (instance == null) {
      val cluster = new Cluster("locator1", "locator2")
      instance = new AtomicReference(cluster)
      scheduleHealthCheck(cluster)
    }
    new GemfireCacheDecorator(instance)
  }

  private def scheduleHealthCheck(cluster: ClusterState):Unit = {
    val r = new Runnable() {
      override def run(): Unit = {
        if (!cluster.isHealthy()) {
          val otherCluster = cluster.switchToOtherCluster
          instance.set(otherCluster)
          scheduleHealthCheck(otherCluster)
        } else {
          scheduleHealthCheck(cluster)
        }
      }
    }
    scheduler.schedule(r, 5, TimeUnit.SECONDS) //schedule once
  }

  trait ClusterState {
    def switchToOtherCluster:ClusterState
    def clientCacheInstance:GemFireCache
    def isHealthy():Boolean
  }

  class Cluster(locator:String, otherLocator:String) extends ClusterState {

    lazy val cacheInstance = createClientCache(locator)

    def clientCacheInstance = cacheInstance

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

    override def switchToOtherCluster: ClusterState = {
      println(s"Closing cache with ${locator} and switching to ${otherLocator}")
      this.close() //make sure client cache is closed
      new Cluster(otherLocator, locator)
    }

    override def isHealthy(): Boolean = clusterIsHealthy()

    def close() = cacheInstance.close()

    def clusterIsHealthy() = {
      println(s"Checking if cluster with ${locator} is healthy")

      import org.apache.geode.cache.client.internal.PoolImpl

      import scala.collection.JavaConverters._

      val pool = cacheInstance.asInstanceOf[ClientCache].getDefaultPool().asInstanceOf[PoolImpl]
      val source = pool.getConnectionSource
      val locators: Seq[InetSocketAddress] = source.getOnlineLocators.asScala.toSeq
      val servers = source.getAllServers.asScala.toSeq

      atLeastOneLocatorIsUp(locators) && allServersAreUp(servers)
    }

    def allServersAreUp(servers:Seq[_]):Boolean = {
      println(s"Checking if cluster with ${locator} has ${servers.size} servers up")
      servers.size == 2
    }
    def atLeastOneLocatorIsUp(locators:Seq[_]):Boolean = {
      println(s"Checking if cluster with ${locator} has ${locators.size} locators up")
      locators.size >= 1
    }

    private def closeAndCreateCobCache = {
      cacheInstance.close()
    }

    private def getPool(r: Region[Any, Any])
    {
      val poolName = r.getAttributes.getPoolName
      PoolManager.find(poolName).asInstanceOf[PoolImpl]
    }
  }
}
