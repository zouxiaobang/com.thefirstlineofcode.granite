package com.firstlinecode.granite.framework.core.repository;

public interface IRepository {
	public static final String GRANITE_FRAMEWORK_COMPONENT_COLLECTOR = "granite.framework.component.collector";
	public static final String SEPARATOR_COMPONENT_DEPENDENCY_PATH = "$";
	
	void init();
	void destroy();
	
	void addServiceListener(IServiceListener listener);
	void removeServiceListener(IServiceListener listener);
}
