package com.firstlinecode.granite.cluster.dba.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.annotation.PreDestroy;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import com.firstlinecode.granite.cluster.dba.IDbInitializer;
import com.firstlinecode.granite.framework.core.commons.osgi.IContributionTracker;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.commons.utils.IoUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

@Component("database")
public class DatabaseFactoryBean implements BundleContextAware, FactoryBean<MongoDatabase> {
	private static final String KEY_GRANITE_DB_INITIALIER = "Granite-DB-Initializer";
	
	private BundleContext bundleContext;
	private volatile MongoClient client;
	private String dbName;
	private IContributionTracker tracker;
	
	@Override
	public MongoDatabase getObject() throws Exception {
		if (client != null) {
			return client.getDatabase(dbName);
		}
		
		synchronized (this) {
			if (client != null)
				return client.getDatabase(dbName);
			
			File dbConfigFile = new File(OsgiUtils.getGraniteConfigDir(bundleContext), "db.ini");
			Properties properties = new Properties();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(dbConfigFile));
				properties.load(reader);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(String.format("DB configuration file %s not found.", dbConfigFile), e);
			} catch (IOException e) {
				throw new RuntimeException(String.format("Can't reader DB configuration file %s.", dbConfigFile), e);
			} finally {
				IoUtils.closeIO(reader);
			}
			
			List<ServerAddress> serverAddresses = getServerAddresses(properties.getProperty("addresses"));
			String dbName = properties.getProperty("db.name");
			String userName = properties.getProperty("user.name");
			String password = properties.getProperty("password");
			
			if (serverAddresses == null || serverAddresses.isEmpty()) {
				throw new RuntimeException("Invalid DB configuration. DB addresses is null.");
			}
			
			if (dbName == null) {
				throw new RuntimeException("Invalid DB configuration. DB name is null.");
			}
			
			if (userName == null) {
				throw new RuntimeException("Invalid DB configuration. User name is null.");
			}
			
			if (password == null) {
				throw new RuntimeException("Invalid DB configuration. Password is null.");
			}
			
			MongoCredential credential = MongoCredential.createCredential(userName, dbName, password.toCharArray());
			client = new MongoClient(serverAddresses, credential, new MongoClientOptions.Builder().build());
			this.dbName = dbName;
			
			MongoDatabase database = client.getDatabase(dbName);
			tracker = new DbInitializerTracker(database);
			OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_DB_INITIALIER, tracker);
			
			return database;
		}
	}

	private List<ServerAddress> getServerAddresses(String sAddresses) {
		StringTokenizer st = new StringTokenizer(sAddresses, ",");
		List<ServerAddress> serverAddresses = new ArrayList<>();
		
		while (st.hasMoreTokens()) {
			String sServerAddress = st.nextToken();
			int colonIndex = sServerAddress.indexOf(':');
			if (colonIndex == -1) {
				throw new RuntimeException(String.format("Invalid DB addresses: %s", sAddresses));
			}
			
			String host = sServerAddress.substring(0, colonIndex).trim();
			int port;
			try {
				port = Integer.parseInt(sServerAddress.substring(colonIndex + 1, sServerAddress.length()));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("Invalid DB addresses: %s", sAddresses), e);
			}
			
			serverAddresses.add(new ServerAddress(host, port));
		}
		
		return serverAddresses;
	}

	@Override
	public Class<?> getObjectType() {
		return MongoDatabase.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	private class DbInitializerTracker implements IContributionTracker {
		private MongoDatabase database;
		
		public DbInitializerTracker(MongoDatabase database) {
			this.database = database;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void found(Bundle bundle, String contribution) throws Exception {
			Class<?> clazz = null;
			try {
				clazz = bundle.loadClass(contribution);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(String.format("Can't load DB initializer %s.", contribution), e);
			}
			
			if (!IDbInitializer.class.isAssignableFrom(clazz)) {
				throw new RuntimeException(String.format("DB initializer %s must implement %s.", contribution, IDbInitializer.class.getName()));
			}
			
			IDbInitializer initializer = (IDbInitializer)clazz.newInstance();
			initializer.initialize(database);
		}

		@Override
		public void lost(Bundle bundle, String contribution) throws Exception {}
		
	}

	@PreDestroy
	public void destroyClient() {
		OsgiUtils.stopTrackContribution(bundleContext, tracker);
		
		if (client != null) {
			client.close();
		}
	}
	
}
