package com.firstlinecode.granite.framework.core.repository;

import java.util.List;

public interface IDependencyInfo {
	String getId();
	String getBareId();
	boolean isAvailable();
	void addBindedComponent(IComponentInfo component);
	void removeBindedComponent(IComponentInfo component);
	List<IComponentInfo> getBindedComponents();
	void injectComponent(Object parent, Object component);
	IDependencyInfo getAliasDependency(String alias, int bindedComponentsCount);
}
