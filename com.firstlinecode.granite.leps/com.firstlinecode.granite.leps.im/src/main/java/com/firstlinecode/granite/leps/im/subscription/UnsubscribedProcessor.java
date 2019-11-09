package com.firstlinecode.granite.leps.im.subscription;

import com.firstlinecode.basalt.leps.im.subscription.Unsubscribed;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;

public class UnsubscribedProcessor implements IXepProcessor<Iq, Unsubscribed> {
	@Dependency("subscription.protocols.processor")
	private SubscriptionProtocolsProcessor delegate;

	@Override
	public void process(IProcessingContext context, Iq iq, Unsubscribed unsubscribed) {
		delegate.processUnsubscribed(context, iq, unsubscribed);
	}

}
