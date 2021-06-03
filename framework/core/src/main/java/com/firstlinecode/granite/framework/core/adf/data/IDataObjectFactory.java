package com.firstlinecode.granite.framework.core.adf.data;

public interface IDataObjectFactory {
	<K, V extends K> V create(Class<K> clazz);
}
