package com.firstlinecode.granite.framework.core.repository;

import org.pf4j.ExtensionPoint;

public interface IComponentContributor extends ExtensionPoint {
	Class<?>[] getComponentClasses();
}
