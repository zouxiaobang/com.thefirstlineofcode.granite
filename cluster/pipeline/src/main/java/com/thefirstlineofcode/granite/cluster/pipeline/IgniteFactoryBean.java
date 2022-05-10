package com.thefirstlineofcode.granite.cluster.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.processors.cache.persistence.filename.PdsConsistentIdProcessor;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.beans.factory.FactoryBean;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.granite.cluster.pipeline.ignite.config.ClusteringConfig;
import com.thefirstlineofcode.granite.cluster.pipeline.ignite.config.Discovery;
import com.thefirstlineofcode.granite.cluster.pipeline.ignite.config.ResourcesStorage;
import com.thefirstlineofcode.granite.cluster.pipeline.ignite.config.SessionsStorage;
import com.thefirstlineofcode.granite.cluster.pipeline.ignite.config.StorageGlobal;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.session.ISession;
import com.thefirstlineofcode.granite.framework.core.utils.IoUtils;

public class IgniteFactoryBean implements FactoryBean<Ignite>, IServerConfigurationAware {
	private static final String CONFIGURATION_DIR_CLUSTER = "cluster";
	private static final String PROPERTY_KEY_NODE_TYPE = "granite.node.type";
	private static final String PROPERTY_KEY_MGTNODE_IP = "granite.mgtnode.ip";
	
	private ClusteringConfig clusteringConfig;
	private IgniteConfiguration configuration;
	private DataStorageConfiguration dataStorageConfiguration;
	
	private IServerConfiguration serverConfiguration;
	
	@Override
	public Ignite getObject() {
		return startIgnite();
	}
	
	@Override
	public Class<?> getObjectType() {
		return Ignite.class;
	}
	
	private Ignite startIgnite() {
		configureJavaUtilLogging();
		
		IgniteConfiguration igniteConfiguration = configureIgnite();
		Ignite ignite = Ignition.start(igniteConfiguration);
		
		if (isSessionPersistenceEnabled()) {
			ignite.active(true);
		}
		
		return ignite;
	}
	
	private void configureJavaUtilLogging() {
		System.setProperty("java.util.logging.config.file", getClusterConfigurationDir() + "/java_util_logging.ini");
	}

	private boolean isSessionPersistenceEnabled() {
		return clusteringConfig.getSessionsStorage().isPersistenceEnabled()/* || clusteringConfig.getCacheStorage().isPersistenceEnabled()*/;
	}
	
	private IgniteConfiguration configureIgnite() {
		File configFile = new File(getClusterConfigurationDir(), "clustering.ini");
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

	private void configureCaches() {
		configuration.setCacheConfiguration(
				configureResources(clusteringConfig.getResourcesStorage().getBackups()),
				configureSessions(clusteringConfig.getSessionsStorage().getBackups())/*,
				configureCaches()*/
		);
	}

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
			configuration.setWorkDirectory(serverConfiguration.getServerHome() + "/ignite_work");
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
	
	private String getClusterConfigurationDir() {
		return serverConfiguration.getConfigurationDir() + "/" + CONFIGURATION_DIR_CLUSTER;
	}
	
	public void destroy() {
		Ignition.stop(true);
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
	}
}
