package com.firstlinecode.granite.framework.adf.mybatis;

public class Alias {
	public String name;
	public Class<?> type;
	
	public Alias(Class<?> type) {
		this(type, null);
	}
	
	public Alias(Class<?> type, String name) {
		this.type = type;
		this.name = name;
	}
}
