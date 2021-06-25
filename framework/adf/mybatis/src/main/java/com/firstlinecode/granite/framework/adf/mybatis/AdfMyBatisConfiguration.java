package com.firstlinecode.granite.framework.adf.mybatis;

import java.net.URL;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.UrlResource;

import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;

@Configuration
public class AdfMyBatisConfiguration implements IApplicationComponentServiceAware {
	private PluginManager pluginManager;
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
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
	
	private class AdfSqlSessionFactoryBuilder extends SqlSessionFactoryBuilder {
		private static final String PREFIX_NAME_PERSIST_OBJECT_TYPE_COC = "D_";

		@Override
		public SqlSessionFactory build(org.apache.ibatis.session.Configuration configuration) {
			List<IDataContributor> dataContributors = pluginManager.getExtensions(IDataContributor.class);
			if (dataContributors == null || dataContributors.size() == 0) {
				return super.build(configuration);			
			}
			
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
			
			return super.build(configuration);
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
			}
		}
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		pluginManager = appComponentService.getPluginManager();
	}
}
