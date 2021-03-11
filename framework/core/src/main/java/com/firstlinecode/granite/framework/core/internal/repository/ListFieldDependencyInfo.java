package com.firstlinecode.granite.framework.core.internal.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.firstlinecode.granite.framework.core.repository.AbstractDependencyInfo;
import com.firstlinecode.granite.framework.core.repository.IDependencyInfo;


public class ListFieldDependencyInfo extends AbstractDependencyInfo {
	private Field field;

	public ListFieldDependencyInfo(String id, String bareId, Class<?> type, Field field, int bindedComponentsCount) {
		super(id, bareId, null, bindedComponentsCount);
		this.field = field;
	}
	
	@Override
	public String toString() {
		return String.format("List Field Dependency[%s, %s, %s]", id, type, field);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void injectComponent(Object parent, Object component) {
		boolean accessible = field.isAccessible();
		try {
			field.setAccessible(true);
			Object list = field.get(parent);
			
			if (list == null) {
				list = new ArrayList();
				field.set(parent, list);
			}
			
			Method addMethod = list.getClass().getMethod("add", new Class<?>[] {Object.class});
			
			addMethod.invoke(list, component);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't inject component %s to %s.", component, parent), e);
		} finally {
			field.setAccessible(accessible);
		}
	}

	@Override
	public IDependencyInfo getAliasDependency(String alias, int bindedComponentsCount) {
		return new ListFieldDependencyInfo(Repository.getFullDependencyId(alias, bareId), bareId, type,
				field, bindedComponentsCount);
	}
}
