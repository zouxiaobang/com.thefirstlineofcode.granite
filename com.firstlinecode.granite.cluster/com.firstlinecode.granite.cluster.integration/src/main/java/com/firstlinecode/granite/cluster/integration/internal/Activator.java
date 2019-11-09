package com.firstlinecode.granite.cluster.integration.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.processors.cache.persistence.filename.PdsConsistentIdProcessor;
import org.apache.ignite.osgi.IgniteAbstractOsgiContextActivator;
import org.apache.ignite.osgi.classloaders.OsgiClassLoadingStrategyType;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.cluster.integration.RuntimeConfiguration;
import com.firstlinecode.granite.cluster.integration.ignite.config.ClusteringConfig;
import com.firstlinecode.granite.cluster.integration.ignite.config.Discovery;
import com.firstlinecode.granite.cluster.integration.ignite.config.ResourcesStorage;
import com.firstlinecode.granite.cluster.integration.ignite.config.SessionsStorage;
import com.firstlinecode.granite.cluster.integration.ignite.config.StorageGlobal;
import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlanException;
import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlanReader;
import com.firstlinecode.granite.framework.core.IApplication;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.commons.utils.IoUtils;
import com.firstlinecode.granite.framework.core.repository.AbstractComponentInfo;
import com.firstlinecode.granite.framework.core.repository.CreationException;
import com.firstlinecode.granite.framework.core.repository.IComponentCollector;
import com.firstlinecode.granite.framework.core.repository.IComponentInfo;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.session.ISession;

public class Activator extends IgniteAbstractOsgiContextActivator {
	private static final String PROPERTY_KEY_NODE_TYPE = "granite.node.type";
	private static final String PROPERTY_KEY_MGTNODE_IP = "granite.mgtnode.ip";
	
	private BundleContext bundleContext;
	private ClusteringConfig clusteringConfig;
	private IgniteConfiguration configuration;
	private DataStorageConfiguration dataStorageConfiguration;
	private RuntimeConfiguration runtimeConfiguration;
	
	@Override
	public IgniteConfiguration igniteConfiguration() {
		Field bundleCtxField = null;
		try {
			bundleCtxField = IgniteAbstractOsgiContextActivator.class.getDeclaredField("bundleCtx");
			bundleCtxField.setAccessible(true);
			bundleContext = (BundleContext)bundleCtxField.get(this);
		} catch (Exception e) {
			throw new RuntimeException("Can't fetch bundle context.", e);
		} finally {
			if (bundleCtxField != null) {
				bundleCtxField.setAccessible(false);
			}
		}
		
		File configFile = new File(OsgiUtils.getGraniteConfigDir(bundleContext), "clustering.ini");
		if (!configFile.exists()) {
			throw new RuntimeException("Can't get clustering.ini.");
		}
		
		clusteringConfig = new ClusteringConfig();
		clusteringConfig.load(configFile);
		
		configuration = new IgniteConfiguration();
		
		Map<String, Object> userAttributes = new HashMap<>();
		userAttributes.put("ROLE", "appnode-rt");
		userAttributes.put("NODE-TYPE", System.getProperty(PROPERTY_KEY_NODE_TYPE));
		configuration.setUserAttributes(userAttributes);
		
		configureDiscovery();
		configureStorages();
		
		return configuration;
	}

	private void configureStorages() {
		dataStorageConfiguration = configureStorageGlobal();
		configureDataRegions();
		configureCaches();
		
		if (isSessionPersistenceEnabled()) {
			try {
				deletePersistedSessionData();
			} catch (IOException e) {
				throw new RuntimeException("Can't delete persisted data.", e);
			}
		}
	}

	private void deletePersistedSessionData() throws IOException {
		String workDirectory = configuration.getWorkDirectory();
		
		try {
			String walArchivePath = dataStorageConfiguration.getWalArchivePath();
			if (walArchivePath.startsWith("/")) {
				IoUtils.deleteFileRecursively(new File(walArchivePath));
			} else {
				IoUtils.deleteFileRecursively(new File(workDirectory, walArchivePath));
			}
			
			String walPath = dataStorageConfiguration.getWalPath();
			if (walPath.startsWith("/")) {
				IoUtils.deleteFileRecursively(new File(walPath));
			} else {
				IoUtils.deleteFileRecursively(new File(workDirectory, walPath));
			}
			
			String storagePath = dataStorageConfiguration.getStoragePath();
			if (storagePath == null)
				storagePath = PdsConsistentIdProcessor.DB_DEFAULT_FOLDER;
			
			if (storagePath.startsWith("/")) {
				IoUtils.deleteFileRecursively(new File(storagePath));
			} else {
				IoUtils.deleteFileRecursively(new File(workDirectory, storagePath));
			}
		} catch (IOException e) {
			throw new RuntimeException("Can't remove persisted data.", e);
		}
	}

	private boolean isSessionPersistenceEnabled() {
		return clusteringConfig.getSessionsStorage().isPersistenceEnabled()/* || clusteringConfig.getCacheStorage().isPersistenceEnabled()*/;
	}

	/*private DataRegionConfiguration configureCacheDataRegion(CacheStorage cacheStorage) {
		// TODO Auto-generated method stub
		return null;
	}*/
	
	private void configureCaches() {
		configuration.setCacheConfiguration(
				configureResources(clusteringConfig.getResourcesStorage().getBackups()),
				configureSessions(clusteringConfig.getSessionsStorage().getBackups())/*,
				configureCaches()*/
		);
	}

	/*private CacheConfiguration configureCaches() {
		// TODO Auto-generated method stub
		return null;
	}*/
	
	private CacheConfiguration<JabberId, Object[]> configureResources(int backups) {
		CacheConfiguration<JabberId, Object[]> cacheConfiguration = new CacheConfiguration<>();
		cacheConfiguration.setName("resources");
		cacheConfiguration.setDataRegionName(ResourcesStorage.NAME_RESOURCES_STORAGE);
		cacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
		cacheConfiguration.setBackups(backups >= 0 ? backups : 1);
		
		return cacheConfiguration;
	}

	private CacheConfiguration<JabberId, ISession> configureSessions(int backups) {
		CacheConfiguration<JabberId, ISession> cacheConfiguration = new CacheConfiguration<>();
		cacheConfiguration.setName("sessions");
		cacheConfiguration.setDataRegionName(SessionsStorage.NAME_SESSIONS_STORAGE);
		cacheConfiguration.setBackups(backups >= 0 ? backups : 1);
		cacheConfiguration.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS,
				clusteringConfig.getSessionsStorage().getSessionDurationTime())));
		
		return cacheConfiguration;
	}

	private void configureDataRegions() {
		dataStorageConfiguration.setDataRegionConfigurations(
				configureResourcesDataRegion(clusteringConfig.getResourcesStorage()),
				configureSessionsDataRegion(clusteringConfig.getSessionsStorage())/*,
				configureCacheDataRegion(clusteringConfig.getCacheStorage())*/
		);
	}
	
	private DataRegionConfiguration configureResourcesDataRegion(ResourcesStorage resourcesStorage) {
		DataRegionConfiguration dataRegionConfiguration = new DataRegionConfiguration();
		dataRegionConfiguration.setName(ResourcesStorage.NAME_RESOURCES_STORAGE);
		dataRegionConfiguration.setInitialSize(resourcesStorage.getInitSize());
		dataRegionConfiguration.setMaxSize(resourcesStorage.getMaxSize());
		dataRegionConfiguration.setPersistenceEnabled(resourcesStorage.isPersistenceEnabled());
		
		return dataRegionConfiguration;
	}
	
	private DataRegionConfiguration configureSessionsDataRegion(SessionsStorage sessionStorage) {
		DataRegionConfiguration dataRegionConfiguration = new DataRegionConfiguration();
		dataRegionConfiguration.setName(SessionsStorage.NAME_SESSIONS_STORAGE);
		dataRegionConfiguration.setInitialSize(sessionStorage.getInitSize());
		dataRegionConfiguration.setMaxSize(sessionStorage.getMaxSize());
		dataRegionConfiguration.setPersistenceEnabled(sessionStorage.isPersistenceEnabled());
		
		return dataRegionConfiguration;
	}

	private DataStorageConfiguration configureStorageGlobal() {
		StorageGlobal storageGlobal = clusteringConfig.getStorageGlobal();
		
		if (storageGlobal.getWorkDirectory() != null) {
			configuration.setWorkDirectory(storageGlobal.getWorkDirectory());
		} else {
			URL homeUrl = OsgiUtils.getPlatform(bundleContext).getHomeDirectory();
			configuration.setWorkDirectory(homeUrl.getFile() + "/ignite_work");
		}
		
		DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
		dataStorageConfiguration.setPageSize(storageGlobal.getPageSize());
		
		if (storageGlobal.getStoragePath() != null)
			dataStorageConfiguration.setStoragePath(storageGlobal.getStoragePath());
		
		if (storageGlobal.getWalPath() != null)
			dataStorageConfiguration.setWalPath(storageGlobal.getWalPath());
		
		if (storageGlobal.getWalArchivePath() != null)
			dataStorageConfiguration.setWalArchivePath(storageGlobal.getWalArchivePath());
		
		configuration.setDataStorageConfiguration(dataStorageConfiguration);
		
		return dataStorageConfiguration;
	}

	private void configureDiscovery() {
		Discovery discovery = clusteringConfig.getDiscovery();
		TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
		if (discovery != null) {
			Discovery.Strategy strategy = discovery.getStrategy();
			if (strategy == null || strategy == Discovery.Strategy.MULTICAST || strategy == Discovery.Strategy.MULTICAST_AND_STATIC_IP) {
				if (discovery.getMulticastGroup() == null)
					throw new RuntimeException("A multicast group must be specified if you use multicast mode to discover other nodes.");
				
				ipFinder.setMulticastGroup(discovery.getMulticastGroup());
			}
			
			if (strategy == Discovery.Strategy.STATIC_IP || strategy == Discovery.Strategy.MULTICAST_AND_STATIC_IP) {
				if (!discovery.isUseMgtnodeStaticIp() && (discovery.getStaticAddresses() == null || discovery.getStaticAddresses().length == 0)) {
					throw new RuntimeException("A list of static addresses must be specified if you use static ip mode to discover other nodes.");
				}
				
				String mgtnodeIp = System.getProperty(PROPERTY_KEY_MGTNODE_IP);
				ipFinder.setAddresses(getAddresses(discovery.getStaticAddresses(), mgtnodeIp));
			}
		}
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		spi.setIpFinder(ipFinder);
		configuration.setDiscoverySpi(spi);
	}
	
	private Collection<String> getAddresses(String[] addresses, String mgtnodeIp) {
		if (addresses == null && mgtnodeIp == null)
			throw new RuntimeException("A list of static addresses must be specified if you use static ip mode to discovery other nodes.");
		
		if (addresses == null) {
			return Collections.singletonList(mgtnodeIp);
		} else if (mgtnodeIp == null) {
			return Arrays.asList(addresses);
		} else {
			Collection<String> addressesIncludeMgtnodeIp = Arrays.asList(addresses);
			addressesIncludeMgtnodeIp.add(mgtnodeIp);
			
			return addressesIncludeMgtnodeIp;
		}
	}

	@Override
	protected void onAfterStart(BundleContext ctx, Throwable t) {
		String deployFilePath = System.getProperty("granite.deploy.plan.file");
		DeployPlan deployConfiguration;
		try {
			deployConfiguration = new DeployPlanReader().read(new File(deployFilePath).toPath());
		} catch (DeployPlanException e) {
			throw new RuntimeException("Can't read deploy configuration file.", e);
		}
		
		runtimeConfiguration = new RuntimeConfiguration(System.getProperty(PROPERTY_KEY_NODE_TYPE), deployConfiguration);
		
		exportGraniteComponents(ctx);
		exportGraniteAppComponents(ctx);
		
		if (isSessionPersistenceEnabled()) {
			ignite.active(true);
		}
	}

	private void exportGraniteComponents(BundleContext ctx) {
		IComponentCollector componentCollector = OsgiUtils.getFrameworComponentCollector(ctx);
		if (componentCollector != null) {
			componentCollector.componentFound(new IgniteComponentInfo(ctx));
			componentCollector.componentFound(new AppnodeRuntimeConfigurationComponentInfo(ctx));
		}
		
		try {
			ctx.addServiceListener(new ServiceListener() {
				
				@Override
				public void serviceChanged(ServiceEvent event) {
					ServiceReference<?> sr = event.getServiceReference();
					IComponentCollector componentCollector = (IComponentCollector)bundleContext.getService(sr);
					
					if (event.getType() == ServiceEvent.REGISTERED) {
						componentCollector.componentFound(new IgniteComponentInfo(bundleContext));
						componentCollector.componentFound(new AppnodeRuntimeConfigurationComponentInfo(bundleContext));
					}
				}
			}, String.format("(&(%s=%s)(%s=%s))", Constants.OBJECTCLASS, IComponentCollector.class.getName(),
					Constants.SERVICE_INTENTS, IRepository.GRANITE_FRAMEWORK_COMPONENT_COLLECTOR));
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Invalid filter for framework component collector.", e);
		}
	}
	
	private void exportGraniteAppComponents(BundleContext ctx) {
		IComponentCollector componentCollector = OsgiUtils.getAppComponentCollector(ctx);
		if (componentCollector != null) {
			componentCollector.componentFound(new IgniteAppComponentInfo(ctx));
		}
		
		try {
			ctx.addServiceListener(new ServiceListener() {
				
				@Override
				public void serviceChanged(ServiceEvent event) {
					ServiceReference<?> sr = event.getServiceReference();
					IComponentCollector componentCollector = (IComponentCollector)bundleContext.getService(sr);
					
					if (event.getType() == ServiceEvent.REGISTERED) {
						componentCollector.componentFound(new IgniteAppComponentInfo(bundleContext));
					}
				}
			}, String.format("(&(%s=%s)(%s=%s))", Constants.OBJECTCLASS, IComponentCollector.class.getName(),
					Constants.SERVICE_INTENTS, IApplication.GRANITE_APP_COMPONENT_COLLECTOR));
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Invalid filter for application component collector.", e);
		}
	}
	
	private class IgniteAppComponentInfo extends AbstractComponentInfo {
		public IgniteAppComponentInfo(BundleContext bundleContext) {
			super("ignite", Ignite.class, bundleContext, true);
		}

		@Override
		public boolean isService() {
			return false;
		}

		@Override
		public IComponentInfo getAliasComponent(String alias) {
			return null;
		}

		@Override
		protected Object doCreate() throws CreationException {
			return ignite;
		}
		
	}
	
	private class IgniteComponentInfo extends AbstractComponentInfo {
		public IgniteComponentInfo(BundleContext bundleContext) {
			super("cluster.ignite", Ignite.class, bundleContext, true);
		}

		@Override
		public boolean isService() {
			return false;
		}

		@Override
		public IComponentInfo getAliasComponent(String alias) {
			return null;
		}

		@Override
		protected Object doCreate() throws CreationException {
			return ignite;
		}
		
	}
	
	private class AppnodeRuntimeConfigurationComponentInfo extends AbstractComponentInfo {
		public AppnodeRuntimeConfigurationComponentInfo(BundleContext bundleContext) {
			super("cluster.runtime.configuration", RuntimeConfiguration.class, bundleContext, true);
		}

		@Override
		public boolean isService() {
			return false;
		}

		@Override
		public IComponentInfo getAliasComponent(String alias) {
			return null;
		}

		@Override
		protected Object doCreate() throws CreationException {
			return runtimeConfiguration;
		}
		
	}
	
	@Override
	public OsgiClassLoadingStrategyType classLoadingStrategy() {
		return OsgiClassLoadingStrategyType.CONTAINER_SWEEP;
	}

}
