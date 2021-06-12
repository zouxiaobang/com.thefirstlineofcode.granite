package com.firstlinecode.granite.framework.im;

import org.pf4j.ExtensionPoint;

public interface IPresenceProcessorFactory extends ExtensionPoint {
	IPresenceProcessor createProcessor();
}
