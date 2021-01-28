package com.firstlinecode.granite.leps.im.subscription;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

public class AcknowledgementProcessor implements IIqResultProcessor {
	
	@Dependency("subscription.service")
	private ILepSubscriptionService subscriptionService;

	@Override
	public boolean process(IProcessingContext context, Iq iq) {
		if (!iq.getId().startsWith(LepSubscriptionNotification.SUBSCRIPTION_NOTIFICATION_ID_PREFIX))
			return false;
		
		if (subscriptionService.notificationExists(iq.getId())) {
			subscriptionService.removeNotificationById(iq.getId());
			return true;
		}
		
		return false;
	}

}
