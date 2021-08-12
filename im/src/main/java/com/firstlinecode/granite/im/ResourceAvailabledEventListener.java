package com.firstlinecode.granite.im;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.protocol.im.stanza.Presence.Type;
import com.firstlinecode.granite.framework.core.annotations.BeanDependency;
import com.firstlinecode.granite.framework.core.pipeline.stages.event.IEventContext;
import com.firstlinecode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.firstlinecode.granite.framework.im.ISubscriptionService;
import com.firstlinecode.granite.framework.im.ResourceAvailabledEvent;
import com.firstlinecode.granite.framework.im.SubscriptionNotification;
import com.firstlinecode.granite.framework.im.SubscriptionType;

public class ResourceAvailabledEventListener implements IEventListener<ResourceAvailabledEvent> {
	@BeanDependency
	private ISubscriptionService subscriptionService;

	@Override
	public void process(IEventContext context, ResourceAvailabledEvent event) {
		JabberId user = event.getJid();
		List<SubscriptionNotification> notifications = subscriptionService.getNotificationsByUser(user.getNode());
		
		for (SubscriptionNotification notification : notifications) {
			Presence subscription = new Presence();
			subscription.setFrom(JabberId.parse(notification.getContact()));
			subscription.setTo(user);
			
			subscription.setType(subscriptionTypeToPresenceType(notification.getSubscriptionType()));
			
			context.write(subscription);
		}
	}

	private Type subscriptionTypeToPresenceType(SubscriptionType subscriptionType) {
		if (subscriptionType == SubscriptionType.SUBSCRIBE) {
			return Presence.Type.SUBSCRIBE;
		} else if (subscriptionType == SubscriptionType.UNSUBSCRIBE) {
			return Presence.Type.UNSUBSCRIBE;
		} else if (subscriptionType == SubscriptionType.SUBSCRIBED) {
			return Presence.Type.SUBSCRIBED;
		} else { // subscriptionType == SubscriptionTypee.UNSUBSCRIBED
			return Presence.Type.UNSUBSCRIBED;
		}
	}

}
