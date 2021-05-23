package com.firstlinecode.granite.framework.core.commons.utils;

import java.lang.annotation.Annotation;

import org.pf4j.PluginWrapper;

public class ContributionClass<T extends Annotation> {
	private PluginWrapper pluginWrapper;
	private Class<?> type;
	private T annotation;
	
	public ContributionClass(PluginWrapper pluginWrapper, Class<?> type, T annotation) {
		this.pluginWrapper = pluginWrapper;
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

	public PluginWrapper getPluginWrapper() {
		return pluginWrapper;
	}

	public void setPluginWrapper(PluginWrapper pluginWrapper) {
		this.pluginWrapper = pluginWrapper;
	}
	
}
