package com.thefirstlineofcode.granite.cluster.dba;

import org.pf4j.Extension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.client.MongoDatabase;
import com.thefirstlineofcode.granite.framework.adf.spring.ISpringConfiguration;

@Extension
@Configuration
public class DbaConfiguration implements ISpringConfiguration {
	@Bean
	public DatabaseFactoryBean database() {
		return new DatabaseFactoryBean();
	}
	
	@Bean
	public DbInitializationExecutor dbInitialzationExecutor(MongoDatabase database) {
		return new DbInitializationExecutor(database);
	}
}
