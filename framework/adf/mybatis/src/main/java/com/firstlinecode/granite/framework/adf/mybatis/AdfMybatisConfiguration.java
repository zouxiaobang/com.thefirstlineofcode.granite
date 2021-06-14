package com.firstlinecode.granite.framework.adf.mybatis;

import java.net.URL;
import java.util.ArrayList;
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
			List<IPersistObjectsContributor> configurationContributors = pluginManager.getExtensions(IPersistObjectsContributor.class);
			if (configurationContributors == null || configurationContributors.size() == 0) {
				return super.build(configuration);			
			}
			
			for (IPersistObjectsContributor configurationContributor : configurationContributors) {
				Alias[] aliases = configurationContributor.getAliases();
				if (aliases != null) {
					registerAliases(configuration, aliases);
				}
				
				TypeHandlerMap<?>[] typeHandlerMaps = configurationContributor.getTypeHandlerMaps();
				if (typeHandlerMaps != null) {
					registerTypeHandlers(configuration, typeHandlerMaps);
				}
				
				Class<?>[] persistObjectTypes = configurationContributor.getPersistObjectTypes();
				if (persistObjectTypes != null) {
					registerPersistObjectTypeAliases(configuration, persistObjectTypes, aliases);
				}
			}
			
			return super.build(configuration);
		}

		private void registerPersistObjectTypeAliases(org.apache.ibatis.session.Configuration configuration,
				Class<?>[] persistObjectTypes, Alias[] aliases) {
			Class<?>[] registeredAliasTypes = getRegisteredAliasTypes(aliases);
			for (Class<?> persistObjectType : persistObjectTypes) {
				if (!isRegistered(persistObjectType, registeredAliasTypes)) {
					String name = persistObjectType.getSimpleName();
					if (name.startsWith(PREFIX_NAME_PERSIST_OBJECT_TYPE_COC)) {
						name = name.substring(2, name.length());
					}
					
					configuration.getTypeAliasRegistry().registerAlias(name, persistObjectType);
				}
			}
		}

		private boolean isRegistered(Class<?> persistObjectType, Class<?>[] registeredAliasTypes) {
			for (Class<?> aRegisteredAliasType : registeredAliasTypes) {
				if (persistObjectType == aRegisteredAliasType)
					return true;
			}
			
			return false;
		}

		private Class<?>[] getRegisteredAliasTypes(Alias[] aliases) {
			List<Class<?>> registeredAliasTypes = new ArrayList<>();
			if (aliases == null || aliases.length == 0) {
				return registeredAliasTypes.toArray(new Class<?>[0]);
			}
			
			for (Alias alias : aliases) {
				registeredAliasTypes.add(alias.type);
			}
			
			return registeredAliasTypes.toArray(new Class<?>[registeredAliasTypes.size()]);
		}

		private void registerTypeHandlers(org.apache.ibatis.session.Configuration configuration,
				TypeHandlerMap<?>[] typeHandlerMaps) {
			for (TypeHandlerMap<?> typeHandlerMap : typeHandlerMaps) {
				if (typeHandlerMap.type == null) {
					configuration.getTypeHandlerRegistry().register(typeHandlerMap.typeHandlerType);;
				} else {
					configuration.getTypeHandlerRegistry().register(typeHandlerMap.type, typeHandlerMap.typeHandlerType);
				}
			}
		}

		private void registerAliases(org.apache.ibatis.session.Configuration configuration, Alias[] aliases) {
			for (Alias alias : aliases) {
				if (alias.name == null) {
					configuration.getTypeAliasRegistry().registerAlias(alias.type);
				} else {
					configuration.getTypeAliasRegistry().registerAlias(alias.name, alias.type);
				}
			}
		}
	}
}
