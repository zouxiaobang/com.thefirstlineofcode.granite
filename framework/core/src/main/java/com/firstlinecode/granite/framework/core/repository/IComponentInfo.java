package com.firstlinecode.granite.framework.core.repository;

public interface IComponentInfo {
	String getId();
	void addDependency(IDependencyInfo dependency);
	void removeDependency(IDependencyInfo dependency);
	IDependencyInfo[] getDependencies();
	boolean isAvailable();
	boolean isService();
	Object create() throws CreationException;
	IComponentInfo getAliasComponent(String alias);
	boolean isSingleton();
}
