package com.firstlinecode.granite.im;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.pipeline.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.im.IMessageProcessor;
import com.firstlinecode.granite.framework.im.IPresenceProcessor;
import com.firstlinecode.granite.framework.im.ISimpleStanzaProcessorsFactory;

@Extension
public class SimpleStanzaProcessorsFactory implements ISimpleStanzaProcessorsFactory {

	@Override
	public IPresenceProcessor[] getPresenceProcessor() {
		return new IPresenceProcessor[] {
			new StandardPresenceProcessor(),
			new SubscriptionProcessor()
		};
	}

	@Override
	public IMessageProcessor[] getMessageProcessor() {
		return new IMessageProcessor[] {
			new StandardMessageProcessor()
		};
	}

	@Override
	public IIqResultProcessor[] getIqResultProcessor() {
		return null;
	}

}
