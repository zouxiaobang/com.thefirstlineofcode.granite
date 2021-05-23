package com.firstlinecode.granite.framework.core.repository;

public interface ISingletonComponentHolder {
	void put(String id, Object component);
	Object get(String id);
}
