package com.firstlinecode.granite.framework.adf.mybatis;

import org.apache.ibatis.type.TypeHandler;

public class TypeHandlerMap<T> {
	public Class<T> type;
	public Class<TypeHandler<T>> typeHandlerType;
	
	public TypeHandlerMap(Class<TypeHandler<T>> typeHandler) {
		this(null, typeHandler);
	}

	public TypeHandlerMap(Class<T> type, Class<TypeHandler<T>> typeHandlerType) {
		this.type = type;
		this.typeHandlerType = typeHandlerType;
	}
	
}
