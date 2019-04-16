package com.gemfire.repository

import java.io.InputStream
import java.net.{InetAddress, InetSocketAddress}
import java.util
import java.util.Properties
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import com.gemfire.internal.GemfireHealthCheckClient
import com.gemfire.repository.ClientCacheProvider.ClusterState
import javax.naming.Context
import org.apache.geode.cache.client.{ClientCacheFactory, ClientRegionShortcut}
import org.apache.geode.cache.control.ResourceManager
import org.apache.geode.cache.query.QueryService
import org.apache.geode.cache.wan.GatewaySenderFactory
import org.apache.geode.cache.{CacheTransactionManager, Declarable, DiskStore, DiskStoreFactory, GemFireCache, Region, RegionAttributes}
import org.apache.geode.distributed.DistributedSystem
import org.apache.geode.distributed.internal.DistributionConfigImpl
import org.apache.geode.internal.cache.tier.sockets.AcceptorImpl
import org.apache.geode.internal.net.SocketCreatorFactory
import org.apache.geode.pdx.{PdxInstance, PdxInstanceFactory, PdxSerializer, ReflectionBasedAutoSerializer}
import org.apache.geode.{CancelCriterion, LogWriter}

class GemfireCacheDecorator(clusterState: AtomicReference[ClusterState]) extends GemFireCache {

  override def getName: String = clusterState.get().clientCacheInstance.getName

  override def getDistributedSystem: DistributedSystem = clusterState.get().clientCacheInstance.getDistributedSystem

  override def getResourceManager: ResourceManager = clusterState.get().clientCacheInstance.getResourceManager

  override def getCopyOnRead: Boolean = clusterState.get().clientCacheInstance.getCopyOnRead

  override def setCopyOnRead(copyOnRead: Boolean): Unit = clusterState.get().clientCacheInstance.setCopyOnRead(copyOnRead)

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
  private val scheduler: ScheduledExecutorService =
    Executors.newScheduledThreadPool(1)
  private var instance: AtomicReference[ClusterState] = _

  def create = {
    if (instance == null) {
      val cluster = new Connected("locator1", "locator2")
      instance = new AtomicReference(cluster)
      scheduleHealthCheck(cluster)
    }
    new GemfireCacheDecorator(instance)
  }

  private def scheduleHealthCheck(cluster: ClusterState): Unit = {
    val r = new Runnable() {
      override def run(): Unit = {
        if (!cluster.shouldSwitchCluster()) {
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
    def switchToOtherCluster: ClusterState

    def clientCacheInstance: GemFireCache

    def shouldSwitchCluster(): Boolean
  }

  class Connected(primaryLocator: String, secondaryLocator: String, shouldCheckForPrimary:Boolean = true) extends ClusterState {
    val healthChecker = createHealthCheckerFor(if(shouldCheckForPrimary) primaryLocator else secondaryLocator)

    lazy val cacheInstance = createClientCache(primaryLocator)

    def clientCacheInstance = cacheInstance

    override def switchToOtherCluster: ClusterState = {
      println(s"Closing cache with ${primaryLocator} and switching to ${secondaryLocator}")
      this.close() //make sure client cache is closed
      new Connected(secondaryLocator, primaryLocator, false)
    }

    def close() = cacheInstance.close()

    override def shouldSwitchCluster(): Boolean = {
      if(shouldCheckForPrimary)
        !clusterIsHealthy()
      else
        clusterIsHealthy()
    }

    def clusterIsHealthy() = {
      println(s"Checking if cluster with ${primaryLocator} is healthy")

      import scala.collection.JavaConverters._


      val locators: Seq[InetSocketAddress] = healthChecker.getOnlineLocators.asScala.toSeq
      val servers = healthChecker.getAllServers.asScala.toSeq

      atLeastOneLocatorIsUp(locators) && allServersAreUp(servers)
    }

    def allServersAreUp(servers: Seq[_]): Boolean = {
      println(s"Checking if cluster with ${primaryLocator} has 2 servers up")
      servers.size == 2
    }

    def atLeastOneLocatorIsUp(locators: Seq[_]): Boolean = {
      println(s"Checking if cluster with ${primaryLocator} has ${locators.size} locators up")
      locators.size >= 1
    }

    private def createClientCache(locatorIp: String): GemFireCache = {
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

    private def closeAndCreateCobCache = {
      cacheInstance.close()
    }

    def createHealthCheckerFor(locator:String) = {
      val hostAddr = InetAddress.getByName(locator);
      val sockAddr = new InetSocketAddress(hostAddr, 9009);
      val config = new DistributionConfigImpl(new Properties())
      SocketCreatorFactory.setDistributionConfig(config)
      val gemfireHealthCheck = new GemfireHealthCheckClient(util.Arrays.asList(sockAddr), "", AcceptorImpl.DEFAULT_HANDSHAKE_TIMEOUT_MS)
      gemfireHealthCheck.initializeFromLocators()
      gemfireHealthCheck
    }
  }

}
