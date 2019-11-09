package com.firstlinecode.granite.lite.leps.im.subscription;

import java.util.List;

import com.firstlinecode.granite.framework.im.Subscription;
import com.firstlinecode.granite.framework.im.SubscriptionNotification;

public interface SubscriptionMapper {
	List<Subscription> selectByUser(String user);
	Subscription selectByUserAndContact(String user, String contact);
	int selectCountByUserAndContact(String user, String contact);
	void insert(Subscription subscription);
	void updateNameAndGroups(String user, String contact, String name, String groups);
	void updateState(String user, String contact, Subscription.State state);
	SubscriptionNotification selectNotificationById(String notificationId);
	List<SubscriptionNotification> selectNotificationsByUser(String user);
	List<SubscriptionNotification> selectNotificationsByUserAndContact(String user, String contact);
	void insertNotification(SubscriptionNotification notification);
	void deleteNotification(SubscriptionNotification notification);
	void deleteNotificationById(String notificationId);
	int selectNotificationCountById(String notificationId);
	void updateNotification(SubscriptionNotification existed);
}
