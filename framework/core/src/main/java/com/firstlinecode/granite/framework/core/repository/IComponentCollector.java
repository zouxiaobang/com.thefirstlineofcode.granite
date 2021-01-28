package com.firstlinecode.granite.framework.core.repository;

public interface IComponentCollector {
	void componentFound(IComponentInfo componentInfo);
	void componentLost(String componentId);
}
