package com.thefirstlineofcode.granite.framework.core.repository;

public interface IRepository {
	public static final String SEPARATOR_COMPONENT_DEPENDENCY_PATH = "$";
	
	void init();
	
	IComponentInfo[] getServiceInfos();
	IComponentInfo getServiceInfo(String serviceId);
	IComponentInfo[] getComponentInfos();
	IComponentInfo getComponentInfo(String componentId);
	String[] getComponentBinding(String componentId);
	
	void registerSingleton(String id, Object component);
	Object getSingleton(String id);
	void removeSingleton(String id);
	Object get(String id);
	
	void setServiceListener(IServiceListener listener);
	void removeServiceListener();
}
