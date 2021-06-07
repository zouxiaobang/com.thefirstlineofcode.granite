package com.firstlinecode.granite.framework.core.pipes.parsing;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.pipes.IPipeExtender;

public interface IPipePreprocessor extends IPipeExtender, ExtensionPoint {
	String beforeParsing(String message);
	Object afterParsing(Object object);
}
