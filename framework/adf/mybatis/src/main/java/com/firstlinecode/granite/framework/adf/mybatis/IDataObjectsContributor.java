package com.firstlinecode.granite.framework.adf.mybatis;

public interface IDataObjectsContributor {
	TypeHandlerMapping<?>[] getTypeHandlerMappings();
	DataObjectMapping<?>[] getDataObjectMappings();
}
