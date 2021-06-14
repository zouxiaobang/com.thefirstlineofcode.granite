package com.firstlinecode.granite.framework.adf.mybatis;

public interface IPersistObjectsContributor {
	Alias[] getAliases();
	TypeHandlerMap<?>[] getTypeHandlerMaps();
	Class<?>[] getPersistObjectTypes();
}
