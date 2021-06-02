package com.firstlinecode.granite.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.firstlinecode.granite.framework.app.spring.ApplicationPluginManager;

@Configuration
public class SpringConfiguration {
	@Bean
	public ApplicationPluginManager pluginManager() {
		return new ApplicationPluginManager();
	}
}
