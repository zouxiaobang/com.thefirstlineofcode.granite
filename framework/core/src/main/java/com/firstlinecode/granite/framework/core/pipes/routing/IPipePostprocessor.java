package com.firstlinecode.granite.framework.core.pipes.routing;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.pipes.IMessage;

public interface IPipePostprocessor extends ExtensionPoint {
	IMessage beforeRouting(IMessage message);
}
