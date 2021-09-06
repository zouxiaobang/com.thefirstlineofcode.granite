package com.firstlinecode.granite.framework.adf.spring.injection;

import org.springframework.context.ApplicationContext;

import com.firstlinecode.granite.framework.core.adf.injection.IDependencyFetcher;

public class SpringBeanFetcher implements IDependencyFetcher {
	private ApplicationContext appContext;
	private Class<?> type;
	private String qualifier;
	
	public SpringBeanFetcher(ApplicationContext appContext, Class<?> type, String qualifier) {
		this.appContext = appContext;
		this.type = type;
		this.qualifier = qualifier;
		
	}

	@Override
	public Object fetch() {
		String[] beanNamesForType = appContext.getBeanNamesForType(type);
		if (beanNamesForType.length == 0)
			throw new IllegalArgumentException(String.format("No bean for type %s be found in application context.",
					type.getName()));
		
		if (beanNamesForType.length == 1 && "".equals(qualifier)) {
			return appContext.getBean(type);
		}
		
		Object bean = appContext.getBean(qualifier);
		if (bean == null)
			throw new IllegalArgumentException(String.format("No bean for name %s be found in application context.",
					qualifier));
				
		return bean;
	}

}
