package com.firstlinecode.granite.framework.adf.spring.injection;

import org.springframework.context.ApplicationContext;

import com.firstlinecode.granite.framework.core.adf.injection.IDependencyFetcher;

public class SpringBeanFetcher implements IDependencyFetcher {
	private ApplicationContext appContext;
	private Class<?> beanType;
	
	public SpringBeanFetcher(ApplicationContext appContext, Class<?> beanType) {
		this.appContext = appContext;
		this.beanType = beanType;
		
	}

	@Override
	public Object fetch() {
		Object bean = appContext.getBean(beanType);
		if (bean == null)
			throw new IllegalArgumentException(String.format("No bean which's bean type is %s in application context.", beanType.getName()));
		
		return bean;
	}

}
