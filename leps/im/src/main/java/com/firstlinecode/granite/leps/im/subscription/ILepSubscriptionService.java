package com.firstlinecode.granite.leps.im.subscription;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.im.ISubscriptionService;
import com.firstlinecode.granite.framework.im.Subscription;
import com.firstlinecode.granite.framework.im.SubscriptionNotification;

public interface ILepSubscriptionService extends ISubscriptionService {
	boolean notificationExists(String notificationId);
	SubscriptionNotification getNotificationById(String notificationId);
	void removeNotificationById(String notificationId);
	void updateStates(JabberId user, JabberId contact, Subscription.State state);
	void updateNotification(SubscriptionNotification existed);
}
