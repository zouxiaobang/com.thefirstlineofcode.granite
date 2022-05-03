package com.thefirstlineofcode.granite.cluster.pipeline;

import org.apache.ignite.Ignite;
import org.pf4j.Extension;
import org.springframework.context.annotation.Bean;

import com.thefirstlineofcode.granite.framework.adf.spring.ISpringConfiguration;

@Extension
public class PipelineConfiguration implements ISpringConfiguration {
	@Bean(destroyMethod = "destroy")
	public IgniteFactoryBean igniteFactoryBean() {
		return new IgniteFactoryBean();
	}
	
	@Bean
	public Ignite ignite(IgniteFactoryBean igniteFactoryBean) {
		return igniteFactoryBean.getObject();
	}
}
