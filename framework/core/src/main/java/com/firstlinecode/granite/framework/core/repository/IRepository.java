package com.firstlinecode.granite.framework.core.repository;

public interface IRepository extends IComponentQueryer {
	public static final String SEPARATOR_COMPONENT_DEPENDENCY_PATH = "$";
	
	void init();
	
	void put(String id, Object component);
	Object get(String id);
	
	void setServiceListener(IServiceListener listener);
	void removeServiceListener();
}
