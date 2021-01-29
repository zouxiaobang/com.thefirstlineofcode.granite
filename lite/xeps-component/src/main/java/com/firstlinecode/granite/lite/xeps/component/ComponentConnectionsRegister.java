package com.firstlinecode.granite.lite.xeps.component;

import java.util.HashMap;
import java.util.Map;

import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.xeps.component.stream.IComponentConnectionsRegister;

@Component("lite.component.connections.register")
public class ComponentConnectionsRegister implements IComponentConnectionsRegister {

	private Map<String, Object> connections = new HashMap<>();

	@Override
	public synchronized void register(String componentName, Object connectionId) {
		if (connections.containsKey(componentName)) {
			throw new RuntimeException(String.format("Duplicated component: %s.", componentName));
		}
		
		connections.put(componentName, connectionId);
	}

	@Override
	public synchronized String unregister(Object connectionId) {
		for (String componentName : connections.keySet()) {
			Object id = connections.get(componentName);
			if (id.equals(connectionId)) {
				connections.remove(componentName);
				
				return componentName;
			}
		}
		
		return null;
	}

	@Override
	public synchronized Object getConnectionId(String componentName) {
		return connections.get(componentName);
	}

}
