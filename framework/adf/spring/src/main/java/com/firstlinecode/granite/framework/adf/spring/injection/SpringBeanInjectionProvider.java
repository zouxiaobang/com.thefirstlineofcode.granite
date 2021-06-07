package com.firstlinecode.granite.framework.adf.spring.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;

import com.firstlinecode.granite.framework.core.adf.injection.IDependencyFetcher;
import com.firstlinecode.granite.framework.core.adf.injection.IInjectionProvider;
import com.firstlinecode.granite.framework.core.annotations.BeanDependency;

public class SpringBeanInjectionProvider implements IInjectionProvider {
	private ApplicationContext appContext;
	
	public SpringBeanInjectionProvider(ApplicationContext appContext) {
		this.appContext = appContext;
	}
	
	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return BeanDependency.class;
	}

	@Override
	public IDependencyFetcher getFetcher(Object mark) {
		return new SpringBeanFetcher(appContext, (Class<?>)mark);
	}

	@Override
	public Object getMark(Object source, Object dependencyAnnotation) {
		BeanDependency beanDependency = (BeanDependency)dependencyAnnotation;
		if (beanDependency.value() != Object.class)
			return beanDependency.value();
		
		if (source instanceof Field) {
			Field field = (Field)source;
			return field.getType();
		} else {
			Method method = (Method)source;
			return method.getParameters()[0].getType();
		}
	}

}
