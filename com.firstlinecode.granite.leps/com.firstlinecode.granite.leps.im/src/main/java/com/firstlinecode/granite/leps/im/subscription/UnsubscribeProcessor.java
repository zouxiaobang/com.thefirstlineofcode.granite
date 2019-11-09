package com.firstlinecode.granite.leps.im.subscription;

import com.firstlinecode.basalt.leps.im.subscription.Unsubscribe;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;

public class UnsubscribeProcessor implements IXepProcessor<Iq, Unsubscribe> {
	@Dependency("subscription.protocols.processor")
	private SubscriptionProtocolsProcessor delegate;

	@Override
	public void process(IProcessingContext context, Iq iq, Unsubscribe unsubscribe) {
		delegate.processUnsubscribe(context, iq, unsubscribe);
	}

}
