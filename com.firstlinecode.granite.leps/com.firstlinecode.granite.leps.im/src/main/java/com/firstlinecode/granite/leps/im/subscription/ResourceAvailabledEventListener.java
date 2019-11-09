package com.firstlinecode.granite.leps.im.subscription;

import java.util.List;

import com.firstlinecode.basalt.leps.im.subscription.Subscribe;
import com.firstlinecode.basalt.leps.im.subscription.Subscribed;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribe;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribed;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.event.IEventContext;
import com.firstlinecode.granite.framework.core.event.IEventListener;
import com.firstlinecode.granite.framework.im.ISubscriptionService;
import com.firstlinecode.granite.framework.im.ResourceAvailabledEvent;
import com.firstlinecode.granite.framework.im.SubscriptionNotification;
import com.firstlinecode.granite.framework.im.SubscriptionType;

public class ResourceAvailabledEventListener implements IEventListener<ResourceAvailabledEvent> {
	@Dependency("subscription.service")
	private ISubscriptionService subscriptionService;

	@Override
	public void process(IEventContext context, ResourceAvailabledEvent event) {
		JabberId user = event.getJid();
		List<SubscriptionNotification> notifications = subscriptionService.getNotificationsByUser(user.getName());
		
		for (SubscriptionNotification notification : notifications) {
			Iq iq;
			if (notification.getSubscriptionType() == SubscriptionType.SUBSCRIBE ||
					notification.getSubscriptionType() == SubscriptionType.UNSUBSCRIBE) {
				iq = new Iq(Iq.Type.SET);
			} else {
				iq = new Iq(Iq.Type.RESULT);
			}
			
			
			iq.setFrom(JabberId.parse(notification.getContact()));
			iq.setTo(user);
			
			String id = null;
			String additionalMessage = null;
			if (notification instanceof LepSubscriptionNotification) {
				LepSubscriptionNotification gsn = (LepSubscriptionNotification)notification;
				id = gsn.getNotificationId();
				additionalMessage = gsn.getAdditionalMessage();
			}
			
			if (id != null) {
				iq.setId(id);
			}
			
			iq.setObject(getSubscriptionObject(notification.getSubscriptionType(),
					additionalMessage));
			
			context.write(iq);
		}
	}

	private Object getSubscriptionObject(SubscriptionType subscriptionType, String additionalMessage) {
		if (subscriptionType == SubscriptionType.SUBSCRIBE) {
			return new Subscribe(additionalMessage);
		} else if (subscriptionType == SubscriptionType.UNSUBSCRIBE) {
			return new Unsubscribe();
		} else if (subscriptionType == SubscriptionType.SUBSCRIBED) {
			return new Subscribed();
		} else { // subscriptionType == SubscriptionType.UNSUBSCRIBED
			return new Unsubscribed(additionalMessage);
		}
	}

}
