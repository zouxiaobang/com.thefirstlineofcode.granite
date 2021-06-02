package com.firstlinecode.granite.framework.core.app.data;

public interface IDataObjectFactory {
	<K, V extends K> V create(Class<K> clazz);
}
