package com.firstlinecode.granite.framework.core.repository;

import java.lang.reflect.Method;

public class MethodDependencyInfo extends AbstractDependencyInfo {
	private Method method;
	
	public MethodDependencyInfo(String id, String bareId, Class<?> type, Method method, int bindedComponentsCount) {
		super(id, bareId, type, bindedComponentsCount);
		
		this.method = method;
	}
	
	@Override
	public String toString() {
		return String.format("Method Dependency[%s, %s, %s, %d]", id, type, method, bindedComponentsCount);
	}

	@Override
	public void injectComponent(Object parent, Object component) {
		try {
			method.invoke(parent, new Object[] {component});
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't inject component %s to %s.", component, parent), e);
		}
	}

	@Override
	public IDependencyInfo getAliasDependency(String alias, int bindedComponentsCount) {
		return new MethodDependencyInfo(Repository.getFullDependencyId(alias, bareId), bareId, type,
				method, bindedComponentsCount);
	}
}
