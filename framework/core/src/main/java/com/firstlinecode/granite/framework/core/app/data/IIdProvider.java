package com.firstlinecode.granite.framework.core.app.data;

public interface IIdProvider<T> {
	void setId(T id);
	T getId();
}
