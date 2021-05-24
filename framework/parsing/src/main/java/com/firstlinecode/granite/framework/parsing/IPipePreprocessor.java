package com.firstlinecode.granite.framework.parsing;

import org.pf4j.ExtensionPoint;

public interface IPipePreprocessor extends ExtensionPoint {
	String beforeParsing(String message);
	Object afterParsing(Object object);
}
