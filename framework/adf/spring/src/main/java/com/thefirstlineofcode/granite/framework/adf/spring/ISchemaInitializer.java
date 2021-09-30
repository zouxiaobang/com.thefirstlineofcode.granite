package com.thefirstlineofcode.granite.framework.adf.spring;

import org.pf4j.ExtensionPoint;

public interface ISchemaInitializer extends ExtensionPoint {
	String getInitialScript();
}
