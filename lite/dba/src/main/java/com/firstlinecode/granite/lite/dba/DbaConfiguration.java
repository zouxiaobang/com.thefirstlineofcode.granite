package com.firstlinecode.granite.lite.dba;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.pf4j.Extension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.firstlinecode.granite.framework.adf.mybatis.IDataContributor;
import com.firstlinecode.granite.framework.adf.spring.ISpringConfiguration;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;

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
			
			for (int i = 0; i < initScripts.length; i++) {
				resourceDatabasePopulator.addScript(new UrlResource(initScripts[i]));			
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
}
