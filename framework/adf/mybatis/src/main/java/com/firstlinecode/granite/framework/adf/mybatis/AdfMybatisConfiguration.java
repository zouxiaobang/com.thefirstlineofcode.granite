package com.firstlinecode.granite.framework.adf.mybatis;

import java.net.URL;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.pf4j.PluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;

import com.firstlinecode.granite.framework.core.platform.IPluginManagerAware;

@Configuration
public class AdfMybatisConfiguration implements IPluginManagerAware {
	private PluginManager pluginManager;
	
	@Bean
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

	@Override
	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
	
	private class AdfSqlSessionFactoryBuilder extends SqlSessionFactoryBuilder {
		private static final String PREFIX_NAME_PERSIST_OBJECT_TYPE_COC = "D_";

		@Override
		public SqlSessionFactory build(org.apache.ibatis.session.Configuration configuration) {
			List<IDataObjectsContributor> configurationContributors = pluginManager.getExtensions(IDataObjectsContributor.class);
			if (configurationContributors == null || configurationContributors.size() == 0) {
				return super.build(configuration);			
			}
			
			for (IDataObjectsContributor configurationContributor : configurationContributors) {				
				TypeHandlerMapping<?>[] typeHandlerMaps = configurationContributor.getTypeHandlerMappings();
				if (typeHandlerMaps != null) {
					registerTypeHandlers(configuration, typeHandlerMaps);
				}
				
				DataObjectMapping<?>[] persistObjectMaps = configurationContributor.getDataObjectMappings();
				if (persistObjectMaps != null) {
					registerPersistObjectAliases(configuration, persistObjectMaps);
				}
			}
			
			return super.build(configuration);
		}

		private void registerPersistObjectAliases(org.apache.ibatis.session.Configuration configuration,
				DataObjectMapping<?>[] persistObjectMaps) {
			for (DataObjectMapping<?> persistObjectMap : persistObjectMaps) {
				if (persistObjectMap.domainType != null) {
					configuration.getTypeAliasRegistry().registerAlias(persistObjectMap.domainType.getSimpleName(),
							persistObjectMap.dataType);					
				} else {
					String name = persistObjectMap.dataType.getSimpleName();					
					if (name.startsWith(PREFIX_NAME_PERSIST_OBJECT_TYPE_COC)) {
						name = name.substring(2, name.length());
					}
					
					configuration.getTypeAliasRegistry().registerAlias(name, persistObjectMap.dataType);
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
			}
		}
	}
}
