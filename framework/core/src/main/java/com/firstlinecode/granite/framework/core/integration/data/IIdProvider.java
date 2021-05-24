package com.firstlinecode.granite.framework.core.integration.data;

public interface IIdProvider<T> {
	void setId(T id);
	T getId();
}
