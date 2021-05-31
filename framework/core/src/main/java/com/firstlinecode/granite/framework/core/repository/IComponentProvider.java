package com.firstlinecode.granite.framework.core.repository;

import org.pf4j.ExtensionPoint;

public interface IComponentProvider extends ExtensionPoint {
	Class<?>[] getComponentClasses();
}
