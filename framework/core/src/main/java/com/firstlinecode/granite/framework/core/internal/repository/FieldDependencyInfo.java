package com.firstlinecode.granite.framework.core.internal.repository;

import java.lang.reflect.Field;

import com.firstlinecode.granite.framework.core.repository.AbstractDependencyInfo;
import com.firstlinecode.granite.framework.core.repository.IDependencyInfo;


public class FieldDependencyInfo extends AbstractDependencyInfo {
	private Field field;
	
	public FieldDependencyInfo(String id, String bareId, Class<?> type, Field field, boolean notNull) {
		super(id, bareId, type, notNull ? 1 : 0);
		
		this.field = field;
	}
	
	@Override
	public String toString() {
		return String.format("Field Dependency[%s, %s, %s]", id, type, field);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void injectComponent(Object parent, Object component) {
		boolean accessible = field.isAccessible();
		
		try {
			field.setAccessible(true);
			field.set(parent, component);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't inject component %s to %s.", component, parent), e);
		} finally {
			field.setAccessible(accessible);
		}
	}

	@Override
	public IDependencyInfo getAliasDependency(String alias, int bindedComponentsCount) {
		return new FieldDependencyInfo(Repository.getFullDependencyId(alias, bareId), bareId, type,
				field, bindedComponentsCount == 1 ? true : false);
	}
}
