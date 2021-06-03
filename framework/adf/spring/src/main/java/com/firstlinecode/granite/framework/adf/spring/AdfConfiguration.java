package com.firstlinecode.granite.framework.adf.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdfConfiguration {
	@Bean
	public AdfPluginManager pluginManager() {
		return new AdfPluginManager();
	}
}
