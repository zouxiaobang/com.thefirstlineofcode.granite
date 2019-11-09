package com.firstlinecode.granite.framework.core.repository;

public interface ISingletonHolder {
	void put(String id, Object component);
	Object get(String id);
}
