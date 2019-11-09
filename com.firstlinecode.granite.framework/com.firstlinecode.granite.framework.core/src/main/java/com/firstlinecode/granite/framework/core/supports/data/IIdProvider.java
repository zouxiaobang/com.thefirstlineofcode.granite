package com.firstlinecode.granite.framework.core.supports.data;

public interface IIdProvider<T> {
	void setId(T id);
	T getId();
}
