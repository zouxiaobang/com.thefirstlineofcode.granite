package com.firstlinecode.granite.xeps.component.stream;

public interface IComponentConnectionsRegister {
	void register(String componentName, Object connectionId);
	String unregister(Object connectionId);
	Object getConnectionId(String componentName);
}
