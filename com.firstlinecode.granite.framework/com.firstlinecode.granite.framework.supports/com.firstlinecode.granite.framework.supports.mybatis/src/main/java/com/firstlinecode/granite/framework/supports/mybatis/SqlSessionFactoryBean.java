package com.firstlinecode.granite.framework.supports.mybatis;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.firstlinecode.basalt.protocol.core.JabberId;

public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>,
		InitializingBean, ApplicationListener<ApplicationEvent>, BundleContextAware,
			BundleListener {
	private SqlSessionFactoryProxy sqlSessionFactoryProxy;
	private boolean failFast;
	private DataSource dataSource;
	private TransactionFactory transactionFactory;
	private String environment = SqlSessionFactoryBean.class.getSimpleName();
	private Interceptor[] plugins;
	private TypeHandler<?>[] typeHandlers;
	private String typeHandlersPackage;
	private Class<?>[] typeAliases;
	private String typeAliasesPackage;
	private Properties configurationProperties;
	private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
	private Resource configLocation;
	private BundleContext bundleContext;
	private Map<Long, Resource[]> bundleContributedMapperResources;
	private Map<Long, NamedTypeAlias[]> bundleContributedTypeAliases;
	private Map<Long, Class<?>[]> bundleContributedTypeHandlers;

	private static final String KEY_GRANITE_MYBATIS_MAPPER_LOCATIONS = "Granite-MyBatis-Mapper-Locations";
	private static final String KEY_GRANITE_MYBATIS_DATA_OBJECTS = "Granite-MyBatis-Data-Objects";
	private static final String KEY_GRANITE_MYBATIS_TYPE_HANDLERS = "Granite-MyBatis-Type-Handlers";

	private static final Logger logger = LoggerFactory
			.getLogger(SqlSessionFactoryBean.class);
	
	private class NamedTypeAlias {
		public String name;
		public Class<?> type;
		
		public NamedTypeAlias(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setTransactionFactory(TransactionFactory transactionFactory) {
		this.transactionFactory = transactionFactory;
	}

	public void setPlugins(Interceptor[] plugins) {
		this.plugins = plugins;
	}

	public void setTypeHandlers(TypeHandler<?>[] typeHandlers) {
		this.typeHandlers = typeHandlers;
	}

	public void setTypeHandlersPackage(String typeHandlersPackage) {
		this.typeHandlersPackage = typeHandlersPackage;
	}

	public void setTypeAliases(Class<?>[] typeAliases) {
		this.typeAliases = typeAliases;
	}

	public void setTypeAliasesPackage(String typeAliasesPackage) {
		this.typeAliasesPackage = typeAliasesPackage;
	}

	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	public void setConfigurationProperties(Properties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (failFast && event instanceof ContextRefreshedEvent) {
			// fail-fast -> check all statements are completed
			this.sqlSessionFactoryProxy.getConfiguration()
					.getMappedStatementNames();
		}
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dataSource, "Property 'dataSource' is required.");
		Assert.notNull(sqlSessionFactoryBuilder,
				"Property 'sqlSessionFactoryBuilder' is required.");

		if (bundleContext == null)
			throw new IllegalStateException("Can't get bundle context.");
		
		bundleContributedMapperResources = new HashMap<>();
		bundleContributedTypeAliases = new HashMap<>();
		bundleContributedTypeHandlers = new HashMap<>();
		
		this.sqlSessionFactoryProxy = new SqlSessionFactoryProxy();
		bundleContext.addBundleListener(this);
		synchronized (this) {
			scanActiveBundles();
			buildSqlSessionFactory();
		}
	}
	
	private void registerPredefinedAliasesAndTypeHandlers(Configuration configuration) {
		configuration.getTypeAliasRegistry().registerAlias("JabberId", JabberId.class);
		configuration.getTypeAliasRegistry().registerAlias("JabberIdTypeHandler", JabberIdTypeHandler.class);
		configuration.getTypeAliasRegistry().registerAlias("EnumTypeHandler", EnumTypeHandler.class);
		configuration.getTypeAliasRegistry().registerAlias("DateTimeTypeHandler", DateTimeTypeHandler.class);
		
		configuration.getTypeHandlerRegistry().register(JabberIdTypeHandler.class);
		configuration.getTypeHandlerRegistry().register(DateTimeTypeHandler.class);
	}

	private void scanActiveBundles() {
		for (Bundle bundle : bundleContext.getBundles()) {
			if (bundle.getState() == Bundle.ACTIVE &&
					!isSelf(bundle)) {
				scanBundle(bundle);
			}
		}
	}

	private boolean isSelf(Bundle bundle) {
		return bundle.getSymbolicName().equals(bundleContext.getBundle().getSymbolicName());
	}

	private void scanBundle(Bundle bundle) {
		Dictionary<String, String> headers = bundle.getHeaders();
		if (headers == null)
			return;
		
		String sMapperLocations = headers.get(KEY_GRANITE_MYBATIS_MAPPER_LOCATIONS);
		if (sMapperLocations != null) {
			Resource[] resources = scanMappers(bundle, sMapperLocations);
			
			if (resources != null && resources.length != 0) {
				bundleContributedMapperResources.put(bundle.getBundleId(), resources);
			}
		}
		
		String sDataObjects = headers.get(KEY_GRANITE_MYBATIS_DATA_OBJECTS);
		if (sDataObjects != null) {
			NamedTypeAlias[] namedTypeAliases = scanTypeAliases(bundle, sDataObjects);
			
			if (namedTypeAliases != null && namedTypeAliases.length != 0) {
				bundleContributedTypeAliases.put(bundle.getBundleId(), namedTypeAliases);
			}
		}
		
		String sTypeHandlers = headers.get(KEY_GRANITE_MYBATIS_TYPE_HANDLERS);
		if (sTypeHandlers != null) {
			Class<?>[] typeHandlers = scanTypeHandlers(bundle, sTypeHandlers);
			
			if (typeHandlers != null && typeHandlers.length != 0) {
				bundleContributedTypeHandlers.put(bundle.getBundleId(), typeHandlers);
			}
		}
	}

	private Class<?>[] scanTypeHandlers(Bundle bundle, String sTypeHandlers) {
		StringTokenizer st = new StringTokenizer(sTypeHandlers, ",");
		Class<?>[] typeHandlers = new Class<?>[st.countTokens()];
		
		int i = 0;
		while (st.hasMoreTokens()) {
			String sTypeHandler = st.nextToken();
			Class<?> typeHandler = null;
			try {
				typeHandler = bundle.loadClass(sTypeHandler);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(String.format("Can't load class %s.", sTypeHandler), e);
			}
			
			if (!TypeHandler.class.isAssignableFrom(typeHandler)) {
				throw new RuntimeException(String.format("%s must implement interface %s.",
						sTypeHandler, TypeHandler.class.getName()));
			}
			
			typeHandlers[i] = typeHandler;
			i++;
		}
		
		return typeHandlers;
	}

	private NamedTypeAlias[] scanTypeAliases(Bundle bundle, String sDataObjects) {
		StringTokenizer st = new StringTokenizer(sDataObjects, ",");
		NamedTypeAlias[] typeAliases = new NamedTypeAlias[st.countTokens()];
		
		int i = 0;
		while (st.hasMoreTokens()) {
			String sDataObject = st.nextToken();
			int equalMarkIndex = sDataObject.indexOf('=');
			
			String sType;
			String name = null;
			if (equalMarkIndex != -1) {
				name = sDataObject.substring(0, equalMarkIndex);
				sType = sDataObject.substring(equalMarkIndex + 1);
			} else {
				sType = sDataObject;
			}
			
			Class<?> type = null;
			try {
				type = bundle.loadClass(sType);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(String.format("Can't load class %s.", sType), e);
			}
			
			if (name == null) {
				name = type.getSimpleName();
				
				if (name.startsWith("D_")) {
					name = name.substring(2);
				}
			}
			
			NamedTypeAlias typeAlias = new NamedTypeAlias(name, type);
			
			typeAliases[i] = typeAlias;
			i++;
		}
		
		return typeAliases;
	}

	private Resource[] scanMappers(Bundle bundle, String sMapperLocations) {
		List<Resource> resources = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(sMapperLocations, ",");
		while (tokenizer.hasMoreElements()) {
			String sLocationAndPattern = tokenizer.nextToken();
			String location = sLocationAndPattern;
			String pattern = "**/*.xml"; // default pattern

			int index = sLocationAndPattern.indexOf(';');
			if (index != -1) {
				location = sLocationAndPattern.substring(0, index);
				if (index != sLocationAndPattern.length() - 1) {
					pattern = sLocationAndPattern.substring(index + 1);
				}
				
				if (pattern.startsWith("pattern=")) {
					pattern = pattern.substring(8, pattern.length());
				}
			}
			
			boolean recurse = false;
			String entryPattern = pattern;
			if (pattern.startsWith("**/*")) {
				recurse = true;
				entryPattern = pattern.substring(3, pattern.length());
			}

			if (!entryPattern.startsWith("*")) {
				throw new IllegalArgumentException(String.format(
					"Illegal mapper locations definition. Bad match pattern: %s.",
								pattern));
			}
			
			scanMappers(bundle, location, entryPattern, recurse, resources);
		}
		
		Resource[] resourcesArray = new Resource[resources.size()];
		return resources.toArray(resourcesArray);
	}

	private void scanMappers(Bundle bundle, String location, String entryPattern,
			boolean recurse, List<Resource> resources) {
		Enumeration<URL> urls = bundle.findEntries(location, entryPattern, recurse);
		if (urls == null)
			return;
		
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			UrlResource resource = new UrlResource(url);
			if (!resources.contains(resource)) {
				resources.add(resource);
			}
		}
	}

	private void buildSqlSessionFactory() throws IOException {
		Configuration configuration;

		XMLConfigBuilder xmlConfigBuilder = null;
		if (this.configLocation != null) {
			xmlConfigBuilder = new XMLConfigBuilder(
					this.configLocation.getInputStream(), null,
					this.configurationProperties);
			configuration = xmlConfigBuilder.getConfiguration();
		} else {
			logger.debug("Property 'configLocation' not specified, using default MyBatis Configuration.");
			
			configuration = new Configuration();
			configuration.setVariables(this.configurationProperties);
		}

		if (StringUtils.hasLength(this.typeAliasesPackage)) {
			String[] typeAliasPackageArray = StringUtils.tokenizeToStringArray(
					this.typeAliasesPackage,
					ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
			for (String packageToScan : typeAliasPackageArray) {
				configuration.getTypeAliasRegistry().registerAliases(
						packageToScan);
				logger.debug("Scanned package: '{}' for aliases.", packageToScan);
			}
		}

		if (!ObjectUtils.isEmpty(this.typeAliases)) {
			for (Class<?> typeAlias : this.typeAliases) {
				configuration.getTypeAliasRegistry().registerAlias(typeAlias);
				logger.debug("Registered type alias: '{}'.", typeAlias);
			}
		}

		if (!ObjectUtils.isEmpty(this.plugins)) {
			for (Interceptor plugin : this.plugins) {
				configuration.addInterceptor(plugin);
				logger.debug("Registered plugin: '{}'.", plugin);
			}
		}

		if (StringUtils.hasLength(this.typeHandlersPackage)) {
			String[] typeHandlersPackageArray = StringUtils
					.tokenizeToStringArray(
							this.typeHandlersPackage,
							ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
			for (String packageToScan : typeHandlersPackageArray) {
				configuration.getTypeHandlerRegistry().register(packageToScan);
				logger.debug("Scanned package: '{}' for type handlers.", packageToScan);
			}
		}

		if (!ObjectUtils.isEmpty(this.typeHandlers)) {
			for (TypeHandler<?> typeHandler : this.typeHandlers) {
				configuration.getTypeHandlerRegistry().register(typeHandler);
				logger.debug("Registered type handler: '{}'.", typeHandler);
			}
		}

		if (xmlConfigBuilder != null) {
			try {
				xmlConfigBuilder.parse();

				logger.debug("Parsed configuration file: '{}'.", this.configLocation);
			} catch (Exception ex) {
				throw new NestedIOException("Failed to parse config resource: "
						+ this.configLocation, ex);
			} finally {
				ErrorContext.instance().reset();
			}
		}

		if (this.transactionFactory == null) {
			this.transactionFactory = new SpringManagedTransactionFactory();
		}

		Environment environment = new Environment(this.environment,
				this.transactionFactory, this.dataSource);
		configuration.setEnvironment(environment);
		
		registerPredefinedAliasesAndTypeHandlers(configuration);
		
		for (NamedTypeAlias typeAlias : getBundleContributedTypeAliases()) {
			if (!configuration.getTypeAliasRegistry().getTypeAliases().containsKey(typeAlias.name)) {
				configuration.getTypeAliasRegistry().registerAlias(typeAlias.name, typeAlias.type);
			}
		}
		
		for (Class<?> typeHandler : getBundleContributedTypeHandlers()) {
			MappedTypes mappedTypes = typeHandler.getAnnotation(MappedTypes.class);
			if (mappedTypes != null) {
				for (Class<?> type : mappedTypes.value()) {
					if (configuration.getTypeHandlerRegistry().getTypeHandler(type) == null) {
						configuration.getTypeHandlerRegistry().register(type, typeHandler);
					}
				}
			} else {
				configuration.getTypeHandlerRegistry().register(typeHandler);
			}
		}
		
		for (Resource mapperLocation : getBundleContributedMapperLocations()) {
			if (mapperLocation == null) {
				continue;
			}

			// this block is a workaround for issue
			// http://code.google.com/p/mybatis/issues/detail?id=235
			// when running MyBatis 3.0.4. But not always works.
			// Not needed in 3.0.5 and above.
			String path;
			if (mapperLocation instanceof ClassPathResource) {
				path = ((ClassPathResource) mapperLocation).getPath();
			} else {
				path = mapperLocation.toString();
			}

			try {
				XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
						mapperLocation.getInputStream(), configuration, path,
						configuration.getSqlFragments());
				xmlMapperBuilder.parse();
			} catch (Exception e) {
				throw new NestedIOException(
						"Failed to parse mapping resource: '" + mapperLocation
								+ "'", e);
			} finally {
				ErrorContext.instance().reset();
			}

			logger.debug("Parsed mapper file: '{}'.", mapperLocation);
		}

		sqlSessionFactoryProxy.setTarget(this.sqlSessionFactoryBuilder
				.build(configuration));
	}
	
	private Resource[] getBundleContributedMapperLocations() {
		List<Resource> all = new ArrayList<>();
		for (Resource[] resources : bundleContributedMapperResources.values()) {
			for (Resource resource : resources) {
				all.add(resource);
			}
		}
		
		return all.toArray(new Resource[all.size()]);
	}
	
	private NamedTypeAlias[] getBundleContributedTypeAliases() {
		List<NamedTypeAlias> all = new ArrayList<>();
		for (NamedTypeAlias[] typeAliases : bundleContributedTypeAliases.values()) {
			for (NamedTypeAlias typeAlias : typeAliases) {
				all.add(typeAlias);
			}
		}
		
		return all.toArray(new NamedTypeAlias[all.size()]);
	}
	
	private Class<?>[] getBundleContributedTypeHandlers() {
		List<Class<?>> all = new ArrayList<>();
		for (Class<?>[] typeHandlers : bundleContributedTypeHandlers.values()) {
			for (Class<?> typeHandler : typeHandlers) {
				all.add(typeHandler);
			}
		}
		
		return all.toArray(new Class<?>[all.size()]);
	}

	@Override
	public SqlSessionFactory getObject() throws Exception {
		if (this.sqlSessionFactoryProxy == null) {
			afterPropertiesSet();
		}

		return this.sqlSessionFactoryProxy;
	}

	@Override
	public Class<?> getObjectType() {
		return this.sqlSessionFactoryProxy == null ? SqlSessionFactory.class
				: this.sqlSessionFactoryProxy.getClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	private class SqlSessionFactoryProxy implements SqlSessionFactory {
		private SqlSessionFactory target;

		public void setTarget(SqlSessionFactory target) {
			this.target = target;
		}

		@Override
		public SqlSession openSession() {
			return target.openSession();
		}

		@Override
		public SqlSession openSession(boolean autoCommit) {
			return target.openSession(autoCommit);
		}

		@Override
		public SqlSession openSession(Connection connection) {
			return target.openSession(connection);
		}

		@Override
		public SqlSession openSession(TransactionIsolationLevel level) {
			return target.openSession(level);
		}

		@Override
		public SqlSession openSession(ExecutorType execType) {
			return target.openSession(execType);
		}

		@Override
		public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
			return target.openSession(execType, autoCommit);
		}

		@Override
		public SqlSession openSession(ExecutorType execType,
				TransactionIsolationLevel level) {
			return target.openSession(execType, level);
		}

		@Override
		public SqlSession openSession(ExecutorType execType,
				Connection connection) {
			return target.openSession(execType, connection);
		}

		@Override
		public Configuration getConfiguration() {
			return target.getConfiguration();
		}

	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STARTED) {
			if (isSelf(event.getBundle()))
				return;
			
			synchronized (this) {
				scanBundle(event.getBundle());
				try {
					buildSqlSessionFactory();
				} catch (IOException e) {
					throw new RuntimeException("Can't build SqlSessionFactory.", e);
				}
			}
		} else if (event.getType() == BundleEvent.STOPPED) {
			if (isSelf(event.getBundle()))
				return;
			
			synchronized (this) {
				bundleContributedMapperResources.remove(event.getBundle().getBundleId());
				bundleContributedTypeAliases.remove(event.getBundle().getBundleId());
				bundleContributedTypeHandlers.remove(event.getBundle().getBundleId());
				try {
					buildSqlSessionFactory();
				} catch (IOException e) {
					throw new RuntimeException("Can't build SqlSessionFactory.", e);
				}
			}
		} else {
			// do nothing
		}
	}
}

