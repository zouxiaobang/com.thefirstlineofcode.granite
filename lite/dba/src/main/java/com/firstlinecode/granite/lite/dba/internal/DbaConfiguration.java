package com.firstlinecode.granite.lite.dba.internal;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.pf4j.Extension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;

import com.firstlinecode.granite.framework.adf.spring.ISpringConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;

@Extension
@Configuration
public class DbaConfiguration implements ISpringConfiguration, IServerConfigurationAware {
	private int hSqlPort;
	
	@Bean
	public HSqlServer hSqlServer() {
		return new HSqlServer();
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
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/META-INF/mybatis/configuration.xml"));
		
		SqlSessionFactory sessionFactory;
		try {
			sessionFactory = sqlSessionFactoryBean.getObject();
		} catch (Exception e) {
			throw new RuntimeException("Can't create SQL session factory.", e);
		}
		return sessionFactory;
		
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		hSqlPort = serverConfiguration.getHSqlPort();
	}
}
