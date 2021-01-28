package com.firstlinecode.granite.framework.core.repository;

import org.osgi.framework.BundleContext;

public interface IComponentInfo {
	String getId();
	void addDependency(IDependencyInfo dependency);
	void removeDependency(IDependencyInfo dependency);
	IDependencyInfo[] getDependencies();
	boolean isAvailable();
	boolean isService();
	Object create() throws CreationException;
	BundleContext getBundleContext();
	IComponentInfo getAliasComponent(String alias);
	boolean isSingleton();
}
