package com.firstlinecode.granite.framework.core.pipes.routing;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.pipes.IMessage;
import com.firstlinecode.granite.framework.core.pipes.IPipeExtender;

public interface IPipePostprocessor extends IPipeExtender, ExtensionPoint {
	IMessage beforeRouting(IMessage message);
}
