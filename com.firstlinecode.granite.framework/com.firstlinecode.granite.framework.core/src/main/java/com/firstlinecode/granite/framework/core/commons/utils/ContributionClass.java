package com.firstlinecode.granite.framework.core.commons.utils;

import java.lang.annotation.Annotation;

import org.osgi.framework.BundleContext;

public class ContributionClass<T extends Annotation> {
	private BundleContext bundleContext;
	private Class<?> type;
	private T annotation;
	
	public ContributionClass(BundleContext bundleContext, Class<?> type, T annotation) {
		this.bundleContext = bundleContext;
		this.type = type;
		this.annotation = annotation;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public void setType(Class<?> type) {
		this.type = type;
	}
	
	public T getAnnotation() {
		return annotation;
	}
	
	public void setAnnotation(T annotation) {
		this.annotation = annotation;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
}
