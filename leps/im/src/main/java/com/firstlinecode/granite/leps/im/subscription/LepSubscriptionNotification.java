package com.firstlinecode.granite.leps.im.subscription;

import com.firstlinecode.granite.framework.im.SubscriptionNotification;

public class LepSubscriptionNotification extends SubscriptionNotification {
	public static final String SUBSCRIPTION_NOTIFICATION_ID_PREFIX = "subs";
	
	private String notificationId;
	private String additionalMessage;
	
	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public String getAdditionalMessage() {
		return additionalMessage;
	}

	public void setAdditionalMessage(String additionalMessage) {
		this.additionalMessage = additionalMessage;
	}
	
}
