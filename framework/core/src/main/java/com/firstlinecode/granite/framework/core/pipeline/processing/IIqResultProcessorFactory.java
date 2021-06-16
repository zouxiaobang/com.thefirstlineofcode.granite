package com.firstlinecode.granite.framework.core.pipeline.processing;

import org.pf4j.ExtensionPoint;

public interface IIqResultProcessorFactory extends ExtensionPoint {
	IIqResultProcessor createProcessor();
}
