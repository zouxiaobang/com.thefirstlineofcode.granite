package com.thefirstlineofcode.granite.lite.dba;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.EnumTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.pf4j.Extension;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.datetime.DateTime;
import com.thefirstlineofcode.granite.framework.adf.mybatis.DataObjectMapping;
import com.thefirstlineofcode.granite.framework.adf.mybatis.DateTimeTypeHandler;
import com.thefirstlineofcode.granite.framework.adf.mybatis.IDataContributor;
import com.thefirstlineofcode.granite.framework.adf.mybatis.JabberIdTypeHandler;
import com.thefirstlineofcode.granite.framework.adf.mybatis.TypeHandlerMapping;
import com.thefirstlineofcode.granite.framework.adf.spring.ISpringConfiguration;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;

@Extension
@Configuration
public class DbaConfiguration implements ISpringConfiguration, IServerConfigurationAware, IApplicationComponentServiceAware {
	private int hSqlPort;
	private IApplicationComponentService appComponentService;
	
	@Bean
	public HSqlServer hSqlServer() {
		return new HSqlServer(hSqlPort);
	}
	
	@Bean
	@DependsOn("hSqlServer")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
		dataSource.setUrl(String.format("jdbc:hsqldb:hsql://localhost:%s/granite", hSqlPort));
		dataSource.setUsername("SA");
		dataSource.setPassword("");
		
		try {
			dataSource.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException("Can't create data source.", e);
		}
		
		return dataSource;
	}
	
	@Bean
	public DataSourceTransactionManager txManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
	
	@Bean
	public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
		
		List<IDataContributor> dataContributors = appComponentService.getPluginManager().getExtensions(IDataContributor.class);
		for (IDataContributor dataContributor : dataContributors) {
			URL[] initScripts = dataContributor.getInitScripts();
			if (initScripts == null || initScripts.length == 0)
				continue;
			
			for (URL initScript : initScripts) {
				resourceDatabasePopulator.addScript(new UrlResource(initScript));			
			}
		}
		resourceDatabasePopulator.setContinueOnError(true);
		
		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(dataSource);
		dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
		
		return dataSourceInitializer;
	}
	
	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		hSqlPort = serverConfiguration.getHSqlPort();
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
	@Bean
	@DependsOn("dataSource")
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setSqlSessionFactoryBuilder(new AdfSqlSessionFactoryBuilder());
		
		String sConfigLocation = "META-INF/mybatis/configuration.xml";
		URL configLocationUrl = getClass().getClassLoader().getResource(sConfigLocation);
		if (configLocationUrl == null) {
			throw new RuntimeException(String.format("Can't read MyBatis configuration file. Config location: %s", sConfigLocation));
		}
		sqlSessionFactoryBean.setConfigLocation(new UrlResource(configLocationUrl));
		
		return createSqlSessionFactory(sqlSessionFactoryBean);
		
	}

	private SqlSessionFactory createSqlSessionFactory(SqlSessionFactoryBean sqlSessionFactoryBean) {
		SqlSessionFactory sessionFactory;
		try {
			sessionFactory = sqlSessionFactoryBean.getObject();
		} catch (Exception e) {
			throw new RuntimeException("Can't create SQL session factory.", e);
		}
		
		return sessionFactory;
	}
	
	private class AdfSqlSessionFactoryBuilder extends SqlSessionFactoryBuilder {
		private static final String PREFIX_NAME_PERSIST_OBJECT_TYPE_COC = "D_";

		@Override
		public SqlSessionFactory build(org.apache.ibatis.session.Configuration configuration) {
			loadPredefinedTypeHandlers(configuration);
			
			List<IDataContributor> dataContributors = appComponentService.getPluginManager().getExtensions(IDataContributor.class);
			if (dataContributors == null || dataContributors.size() == 0) {
				return super.build(configuration);			
			}
			
			loadContributedData(configuration, dataContributors);
			return super.build(configuration);
		}

		private void loadContributedData(org.apache.ibatis.session.Configuration configuration,
				List<IDataContributor> dataContributors) {
			for (IDataContributor dataContributor : dataContributors) {				
				TypeHandlerMapping<?>[] typeHandlerMaps = dataContributor.getTypeHandlerMappings();
				if (typeHandlerMaps != null && typeHandlerMaps.length != 0) {
					registerTypeHandlers(configuration, typeHandlerMaps);
				}
				
				DataObjectMapping<?>[] dataObjectMaps = dataContributor.getDataObjectMappings();
				if (dataObjectMaps != null && dataObjectMaps.length != 0) {
					registerDataObjectAliases(configuration, dataObjectMaps);
				}
				
				URL[] mappers = dataContributor.getMappers();
				if (mappers != null && mappers.length != 0) {
					registerMappers(configuration, mappers, dataContributor);
				}
			}
		}

		private void registerMappers(org.apache.ibatis.session.Configuration configuration, URL[] mappers, IDataContributor dataContributor) {
			ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(dataContributor.getClass().getClassLoader());
				for (URL mapper : mappers) {				
					try {
						XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapper.openStream(),
							configuration, mapper.toString(), configuration.getSqlFragments());
						xmlMapperBuilder.parse();
					} catch (Exception e) {						// TODO: handle exception
						throw new RuntimeException(String.format("Failed to parse mapper resource: '%s'.", mapper.toString()), e);
					}
				}
			} finally {
				Thread.currentThread().setContextClassLoader(oldClassLoader);
			}
		}

		private void registerDataObjectAliases(org.apache.ibatis.session.Configuration configuration,
				DataObjectMapping<?>[] dataObjectMaps) {
			for (DataObjectMapping<?> dataObjectMap : dataObjectMaps) {
				if (dataObjectMap.domainType != null) {
					configuration.getTypeAliasRegistry().registerAlias(dataObjectMap.domainType.getSimpleName(),
							dataObjectMap.dataType);					
				} else {
					String name = dataObjectMap.dataType.getSimpleName();					
					if (name.startsWith(PREFIX_NAME_PERSIST_OBJECT_TYPE_COC)) {
						name = name.substring(2, name.length());
					}
					
					configuration.getTypeAliasRegistry().registerAlias(name, dataObjectMap.dataType);
				}
			}
		}

		private void registerTypeHandlers(org.apache.ibatis.session.Configuration configuration,
				TypeHandlerMapping<?>[] typeHandlerMaps) {
			for (TypeHandlerMapping<?> typeHandlerMap : typeHandlerMaps) {
				if (typeHandlerMap.type == null) {
					configuration.getTypeHandlerRegistry().register(typeHandlerMap.typeHandlerType);;
				} else {
					configuration.getTypeHandlerRegistry().register(typeHandlerMap.type, typeHandlerMap.typeHandlerType);
				}
				configuration.getTypeAliasRegistry().registerAlias(typeHandlerMap.typeHandlerType.getSimpleName(), typeHandlerMap.typeHandlerType);
			}
		}

		private void loadPredefinedTypeHandlers(org.apache.ibatis.session.Configuration configuration) {
			configuration.getTypeHandlerRegistry().register(JabberId.class, JabberIdTypeHandler.class);
			configuration.getTypeHandlerRegistry().register(DateTime.class, DateTimeTypeHandler.class);
			
			configuration.getTypeAliasRegistry().registerAlias("JabberId", JabberId.class);
			configuration.getTypeAliasRegistry().registerAlias("JabberIdTypeHandler", JabberIdTypeHandler.class);
			configuration.getTypeAliasRegistry().registerAlias("EnumTypeHandler", EnumTypeHandler.class);
			configuration.getTypeAliasRegistry().registerAlias("DateTimeTypeHandler", DateTimeTypeHandler.class);
		}
	}
}
