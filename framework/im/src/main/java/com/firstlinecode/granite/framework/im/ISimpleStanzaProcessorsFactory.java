package com.firstlinecode.granite.framework.im;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.pipeline.processing.IIqResultProcessor;

public interface ISimpleStanzaProcessorsFactory extends ExtensionPoint {
	IPresenceProcessor[] getPresenceProcessor();
	IMessageProcessor[] getMessageProcessor();
	IIqResultProcessor[] getIqResultProcessor();
}
