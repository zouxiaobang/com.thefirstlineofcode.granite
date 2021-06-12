package com.firstlinecode.granite.framework.im;

import org.pf4j.ExtensionPoint;

public interface IMessageProcessorFactory extends ExtensionPoint {
	IMessageProcessor createProcessor();
}
