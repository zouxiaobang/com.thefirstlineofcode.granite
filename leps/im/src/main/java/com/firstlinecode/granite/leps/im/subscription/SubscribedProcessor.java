package com.firstlinecode.granite.leps.im.subscription;

import com.firstlinecode.basalt.leps.im.subscription.Subscribed;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;

public class SubscribedProcessor implements IXepProcessor<Iq, Subscribed> {
	@Dependency("subscription.protocols.processor")
	private SubscriptionProtocolsProcessor delegate;

	@Override
	public void process(IProcessingContext context, Iq iq, Subscribed subscribed) {
		delegate.processSubscribed(context, iq, subscribed);
	}

}
