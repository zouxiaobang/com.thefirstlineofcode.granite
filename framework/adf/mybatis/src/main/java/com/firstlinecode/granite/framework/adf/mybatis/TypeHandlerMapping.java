package com.firstlinecode.granite.framework.adf.mybatis;

import org.apache.ibatis.type.TypeHandler;

public class TypeHandlerMapping<T> {
	public Class<T> type;
	public Class<TypeHandler<T>> typeHandlerType;
	
	public TypeHandlerMapping(Class<TypeHandler<T>> typeHandler) {
		this(null, typeHandler);
	}

	public TypeHandlerMapping(Class<T> type, Class<TypeHandler<T>> typeHandlerType) {
		this.type = type;
		this.typeHandlerType = typeHandlerType;
	}
	
}
