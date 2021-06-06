package com.firstlinecode.granite.framework.adf.mybatis;

import java.net.URL;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
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
		
		String sConfigLocation = "META-INF/mybatis/configuration.xml";
		URL configLocationUrl = getClass().getClassLoader().getResource(sConfigLocation);
		if (configLocationUrl == null) {
			throw new RuntimeException(String.format("Can't read MyBatis configuration file. Config location: %s", sConfigLocation));
		}
		
		sqlSessionFactoryBean.setConfigLocation(new UrlResource(configLocationUrl));
		
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
}
