package com.firstlinecode.granite.framework.routing;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.pipe.IMessage;

public interface IPipePostprocessor extends ExtensionPoint {
	IMessage beforeRouting(IMessage message);
}
