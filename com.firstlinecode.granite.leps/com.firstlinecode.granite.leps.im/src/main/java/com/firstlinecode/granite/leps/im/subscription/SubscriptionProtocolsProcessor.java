package com.firstlinecode.granite.leps.im.subscription;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.leps.im.subscription.Subscribe;
import com.firstlinecode.basalt.leps.im.subscription.Subscribed;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribe;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribed;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stanza.error.FeatureNotImplemented;
import com.firstlinecode.basalt.protocol.im.roster.Item;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.annotations.AppComponent;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.commons.utils.StanzaCloner;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.supports.data.IDataObjectFactory;
import com.firstlinecode.granite.framework.core.supports.data.IDataObjectFactoryAware;
import com.firstlinecode.granite.framework.im.IResource;
import com.firstlinecode.granite.framework.im.IResourcesService;
import com.firstlinecode.granite.framework.im.Subscription;
import com.firstlinecode.granite.framework.im.Subscription.State;
import com.firstlinecode.granite.framework.im.SubscriptionChanges;
import com.firstlinecode.granite.framework.im.SubscriptionNotification;
import com.firstlinecode.granite.framework.im.SubscriptionType;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.leps.im.roster.RosterOperator;

@AppComponent("subscription.protocols.processor")
public class SubscriptionProtocolsProcessor implements IApplicationConfigurationAware,
		IDataObjectFactoryAware, IConfigurationAware {
	private static final String CONFIGURATION_VALUE_UNIDIRECTIONAL = "unidirectional";
	private static final String CONFIGRATION_VALUE_BIDIRECTIONAL = "bidirectional";
	private static final String CONFIGURATION_KEY_APPROVAL_MODE = "subscription.approval.mode";

	private enum SubscriptionApprovalMode {
		UNIDIRECTIONAL,
		BIDIRECTIONAL
	}
	
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionProtocolsProcessor.class);
	
	@Dependency("subscription.service")
	private ILepSubscriptionService subscriptionService;
	
	@Dependency("authenticator")
	private IAuthenticator authenticator;
	
	@Dependency("roster.operator")
	private RosterOperator rosterOperator;
	
	@Dependency("resources.service")
	private IResourcesService resourcesService;
	
	private IDataObjectFactory dataObjectFactory;
	
	private String domain;
	
	private SubscriptionApprovalMode subscriptionApprovalMode = SubscriptionApprovalMode.BIDIRECTIONAL;
	
	public void processSubscribe(IProcessingContext context, Iq iq, Subscribe subscribe) {
		doProcess(context, iq, SubscriptionType.SUBSCRIBE, subscribe.getMessage());
	}
	
	public void processSubscribed(IProcessingContext context, Iq iq, Subscribed subscribed) {
		doProcess(context, iq, SubscriptionType.SUBSCRIBED, null);
	}
	
	public void processUnsubscribe(IProcessingContext context, Iq iq, Unsubscribe unsubscribe) {
		doProcess(context, iq, SubscriptionType.UNSUBSCRIBE, null);
	}
	
	public void processUnsubscribed(IProcessingContext context, Iq iq, Unsubscribed unsubscribed) {
		doProcess(context, iq, SubscriptionType.UNSUBSCRIBED, unsubscribed.getReason());
	}

	protected boolean doProcess(IConnectionContext context, Iq iq, SubscriptionType type,
			String additionMessage) {
		JabberId user = getFrom(context, iq);
		JabberId contact = iq.getTo();
		
		checkUserAndContact(user, contact);
		
		rosterSetIfNotExist(context, user, contact);
		
		if (processBidirectionalSubscriptionFlow(context, iq, user, contact, type)) {
			return true;
		}
		
		if (processBidirectionalUnsubscriptionFlow(context, iq, user, contact, type)) {
			return true;
		}
		
		processStandardFlows(context, iq, user, contact, type, additionMessage);
		
		return true;
	}

	private boolean processBidirectionalUnsubscriptionFlow(IConnectionContext context, Iq iq, JabberId user,
			JabberId contact, SubscriptionType type) {
		if (!isBidirectionalUnsubscriptionFlow(type)) {
			return false;
		}
		
		Subscription userSubscription = subscriptionService.get(user.getName(), contact.getBareIdString());
		Subscription contactSubscription = subscriptionService.get(contact.getName(), user.getBareIdString());
		
		if (userSubscription.getState() != Subscription.State.BOTH ||
				contactSubscription.getState() != Subscription.State.BOTH) {
			return false;
		}
		
		subscriptionService.updateStates(user, contact, Subscription.State.NONE);
		
		deliverSubscriptionToContact(context, user, contact, SubscriptionType.UNSUBSCRIBE, null);
		
		userSubscription = subscriptionService.get(user.getName(), contact.getBareIdString());
		rosterPushIfChanged(context, user.getName(), Subscription.State.BOTH, userSubscription);
		
		contactSubscription = subscriptionService.get(contact.getName(), user.getBareIdString());
		rosterPushIfChanged(context, contact.getName(), Subscription.State.BOTH, contactSubscription);
		
		sendReply(context, iq);
		
		deliverUnavailablePresence(context, user, contact);
		deliverUnavailablePresence(context, contact, user);
		
		return true;
	}
	
	private void deliverUnavailablePresence(IConnectionContext context, JabberId contact, JabberId user) {
		IResource[] contactResources = resourcesService.getResources(contact);
		IResource[] userResources = resourcesService.getResources(user);
		
		Presence unavailable = new Presence(Presence.Type.UNAVAILABLE);
		for (IResource contactResource : contactResources) {
			for (IResource userResource : userResources) {
				Presence cloned = StanzaCloner.clone(unavailable);
				cloned.setFrom(contactResource.getJid());
				cloned.setTo(userResource.getJid());
				
				context.write(cloned);
			}
		}
	}

	private boolean isBidirectionalUnsubscriptionFlow(SubscriptionType subscriptionType) {
		return isBidirectionalSubscriptionMode() && subscriptionType == SubscriptionType.UNSUBSCRIBE;
	}

	private boolean processBidirectionalSubscriptionFlow(IConnectionContext context, Iq iq,
			JabberId user, JabberId contact, SubscriptionType type) {
		if (!isBidirectionalSubscriptionFlow(type)) {
			return false;
		}
		
		Subscription userSubscription = subscriptionService.get(user.getName(), contact.getBareIdString());
		Subscription contactSubscription = subscriptionService.get(contact.getName(), user.getBareIdString());
		
		if ((userSubscription.getState() != Subscription.State.NONE_PENDING_IN &&
				userSubscription.getState() != Subscription.State.NONE_PENDING_IN_OUT) ||
				(contactSubscription.getState() != Subscription.State.NONE_PENDING_OUT &&
					contactSubscription.getState() != Subscription.State.NONE_PENDING_IN_OUT)) {
			return false;
		}
		
		subscriptionService.updateStates(user, contact, Subscription.State.BOTH);
		
		deliverSubscriptionToContact(context, user, contact, SubscriptionType.SUBSCRIBED, null);
		
		userSubscription = subscriptionService.get(user.getName(), contact.getBareIdString());
		rosterPushIfChanged(context, user.getName(), Subscription.State.NONE_PENDING_IN, userSubscription);
		
		contactSubscription = subscriptionService.get(contact.getName(), user.getBareIdString());
		rosterPushIfChanged(context, contact.getName(), Subscription.State.NONE_PENDING_OUT, contactSubscription);
		
		sendReply(context, iq);
		
		deliverAvailablePresence(context, user, contact);
		deliverAvailablePresence(context, contact, user);
		
		return true;
	}

	private void processStandardFlows(IConnectionContext context, Iq iq, JabberId user,
			JabberId contact, SubscriptionType type, String additionMessage) {
		SubscriptionChanges changes = subscriptionService.handleSubscription(user, contact, type);
		
		rosterPushIfChanged(context, user.getName(), changes.getOldUserSubscriptionState(), changes.getUserSubscription());
		rosterPushIfChanged(context, contact.getName(), changes.getOldContactSubscriptionState(), changes.getContactSubscription());
		
		deliverInboundSubscription(context, changes.getOldContactSubscriptionState(),
				changes.getContactSubscription(), user, contact, type, additionMessage);
		
		sendReply(context, iq);
		
		if (type == SubscriptionType.SUBSCRIBED &&
				isStateChanged(changes.getOldContactSubscriptionState(),
					changes.getContactSubscription() == null ? null : changes.getContactSubscription().getState())) {
			deliverAvailablePresence(context, user, contact);
		}
		
		if (type == SubscriptionType.UNSUBSCRIBE &&
				isStateChanged(changes.getOldContactSubscriptionState(),
					changes.getContactSubscription() == null ? null : changes.getContactSubscription().getState())) {
			deliverUnavailablePresence(context, user, contact);
		}
	}

	private boolean isBidirectionalSubscriptionFlow(SubscriptionType subscriptionType) {
		return isBidirectionalSubscriptionMode() && subscriptionType == SubscriptionType.SUBSCRIBED;
	}

	private boolean isBidirectionalSubscriptionMode() {
		return subscriptionApprovalMode == SubscriptionApprovalMode.BIDIRECTIONAL;
	}

	private void sendReply(IConnectionContext context, Iq iq) {
		if (context.getJid().getResource() != null) {
			context.write(new Iq(Iq.Type.RESULT, iq.getId()));
		}
	}

	private void deliverAvailablePresence(IConnectionContext context, JabberId contact, JabberId user) {
		IResource[] contactResources = resourcesService.getResources(contact);
		IResource[] userResources = resourcesService.getResources(user);
		
		for (IResource contactResource : contactResources) {
			Presence presence = contactResource.getBroadcastPresence();
			if (presence == null)
				continue;
			
			for (IResource userResource : userResources) {	
				Presence available = StanzaCloner.clone(presence);
				
				available.setFrom(contactResource.getJid());
				available.setTo(userResource.getJid());
				
				context.write(available);
			}
		}
	}

	private boolean isStateChanged(State oldState, State newState) {
		if (oldState == null || newState == null)
			return false;
		
		return oldState != newState;
	}

	private void deliverInboundSubscription(IConnectionContext context, State oldContactState, Subscription contactSubscription,
			JabberId user, JabberId contact, SubscriptionType subscriptionType, String additionMessage) {
		if (subscriptionType == SubscriptionType.SUBSCRIBE) {
			if (oldContactState == State.FROM ||
					oldContactState == State.FROM_PENDING_OUT ||
					oldContactState == State.BOTH) {
				autoReply(context, user, contact, subscriptionType);
			} else {
				deliverSubscriptionToContact(context, user, contact, subscriptionType, additionMessage);
			}
		} else if (subscriptionType == SubscriptionType.UNSUBSCRIBE) {
			if (oldContactState == State.NONE_PENDING_IN ||
					oldContactState == State.NONE_PENDING_IN_OUT ||
						oldContactState == State.TO_PENDING_IN ||
							oldContactState == State.FROM ||
								oldContactState == State.FROM_PENDING_OUT ||
									oldContactState == State.BOTH) {
				deliverSubscriptionToContact(context, user, contact, subscriptionType, additionMessage);
				autoReply(context, user, contact, subscriptionType);
			}
		} else if (subscriptionType == SubscriptionType.SUBSCRIBED) {
			if (oldContactState == State.NONE_PENDING_OUT ||
					oldContactState == State.NONE_PENDING_IN_OUT ||
						oldContactState == State.FROM_PENDING_OUT) {
				deliverSubscriptionToContact(context, user, contact, subscriptionType, additionMessage);
			}
		} else { // subscriptionType == SubscriptionType.UNSUBSCRIBED
			if (oldContactState == State.NONE_PENDING_OUT ||
					oldContactState == State.NONE_PENDING_IN_OUT ||
						oldContactState == State.TO ||
							oldContactState == State.TO_PENDING_IN ||
								oldContactState == State.FROM_PENDING_OUT ||
									oldContactState == State.BOTH) {
				deliverSubscriptionToContact(context, user, contact, subscriptionType, additionMessage);
			}
		}
	}
	
	private void deliverSubscriptionToContact(IConnectionContext context, JabberId user,
			JabberId contact, SubscriptionType subscriptionType, String additionMessage) {
		String notificationId = Stanza.generateId(LepSubscriptionNotification.SUBSCRIPTION_NOTIFICATION_ID_PREFIX);
		saveNotification(notificationId, contact.getName(), user.getBareIdString(), subscriptionType, additionMessage);
		
		IResource[] resources = resourcesService.getResources(contact);
		for (IResource resource : resources) {
			Iq subscription;
			if (subscriptionType == SubscriptionType.SUBSCRIBE ||
					subscriptionType == SubscriptionType.UNSUBSCRIBE) {
				subscription = new Iq(Iq.Type.SET, notificationId);
			} else {
				subscription = new Iq(Iq.Type.RESULT, notificationId);
			}
			
			subscription.setFrom(user.getBareId());
			subscription.setTo(resource.getJid());
			subscription.setObject(subscriptionTypeToObject(subscriptionType, additionMessage));
			
			context.write(subscription);
		}
	}
	
	private void saveNotification(String notificationId, String user, String contact, SubscriptionType subscriptionType, String additionalMessage) {
		List<SubscriptionNotification> notifications = subscriptionService.getNotificationsByUserAndContact(user, contact);
		
		LepSubscriptionNotification existed = null;
		for (SubscriptionNotification notification : notifications) {
			if (notification.getSubscriptionType() == subscriptionType) {
				existed = (LepSubscriptionNotification)notification;
			}
		}
		
		if (existed != null) {
			if (additionalMessage != null) {
				existed.setAdditionalMessage(additionalMessage);
			}
			
			subscriptionService.updateNotification(existed);
		} else {
			LepSubscriptionNotification notification = dataObjectFactory.create(LepSubscriptionNotification.class);
			notification.setNotificationId(notificationId);
			notification.setUser(user);
			notification.setContact(contact);
			notification.setSubscriptionType(subscriptionType);
			if (additionalMessage != null) {
				notification.setAdditionalMessage(additionalMessage);
			}
			
			subscriptionService.addNotification(notification);
		}
	}

	private Object subscriptionTypeToObject(SubscriptionType type, String additionalMessage) {
		if (type == SubscriptionType.SUBSCRIBE) {
			return new Subscribe(additionalMessage);
		} else if (type == SubscriptionType.SUBSCRIBED) {
			return new Subscribed();
		} else if (type == SubscriptionType.UNSUBSCRIBE) {
			return new Unsubscribe();
		} else { // (type == SubscriptionType.UNSUBSCRIBED)
			return new Unsubscribed(additionalMessage);
		}
	}

	private void autoReply(IConnectionContext context, JabberId user, JabberId contact,
			SubscriptionType subscriptionType) {
		IResource[] resources = resourcesService.getResources(user);
		for (IResource resource : resources) {
			Iq iq = new Iq();
			iq.setFrom(contact);
			iq.setTo(resource.getJid());
			
			iq.setObject(getAutoReplySubscriptionObject(subscriptionType));
			
			context.write(iq);
		}
	}

	private Object getAutoReplySubscriptionObject(SubscriptionType subscriptionType) {
		if (subscriptionType == SubscriptionType.SUBSCRIBE) {
			return new Subscribed();
		} else { // subscriptionType == SubscriptionType.UNSUBSCRIBED
			return new Unsubscribed();
		}
	}

	private void rosterPushIfChanged(IConnectionContext context, String user,
			Subscription.State oldState, Subscription subscription) {
		if (!rosterItemStateChanged(oldState, subscription.getState()))
			return;
		
		Roster roster = rosterOperator.subscriptionToRoster(subscription);
		rosterOperator.rosterPush(context, user, roster);
	}
	
	private boolean rosterItemStateChanged(Subscription.State oldState, Subscription.State newState) {
		if (oldState == Subscription.State.NONE && newState == Subscription.State.NONE_PENDING_IN)
			return false;
		
		if (oldState == Subscription.State.NONE_PENDING_IN && newState == Subscription.State.NONE)
			return false;
		
		if (oldState == Subscription.State.TO && newState == Subscription.State.TO_PENDING_IN)
			return false;
		
		if (oldState == Subscription.State.TO_PENDING_IN && newState == Subscription.State.TO)
			return false;
		
		return true;
	}

	private void rosterSetIfNotExist(IConnectionContext context, JabberId user, JabberId contact) {
		Subscription subscription = subscriptionService.get(user.getName(), contact.getBareIdString());
		if (subscription == null) {
			Item item = new Item();
			item.setJid(contact);
			item.setSubscription(Item.Subscription.NONE);
			
			Roster roster = new Roster();
			roster.addOrUpdate(item);
			
			rosterOperator.rosterSet(context, user, roster);
		}
	}

	private JabberId getFrom(IConnectionContext context, Iq iq) {
		JabberId from;
		if (context.getJid().getResource() == null) {
			from = iq.getFrom();
		} else {
			from = context.getJid();
		}
		
		return from;
	}

	private void checkUserAndContact(JabberId from, JabberId to) {
		if (from == null) {
			throw new ProtocolException(new BadRequest("Null subscription user."));
		}
		
		if (to == null) {
			throw new ProtocolException(new BadRequest("Null subscription contact."));
		}
		
		if (!domain.equals(from.getDomain()) || !domain.equals(to.getDomain())) {
			throw new ProtocolException(new FeatureNotImplemented("Feature S2S not implemented."));
		}
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		this.domain = appConfiguration.getDomainName();
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		String sApprovalMode = configuration.getString(CONFIGURATION_KEY_APPROVAL_MODE, CONFIGRATION_VALUE_BIDIRECTIONAL);
		if (sApprovalMode.equals(CONFIGURATION_VALUE_UNIDIRECTIONAL)) {
			subscriptionApprovalMode = SubscriptionApprovalMode.UNIDIRECTIONAL;
		} else if (sApprovalMode.equals(CONFIGRATION_VALUE_BIDIRECTIONAL)) {
			subscriptionApprovalMode = SubscriptionApprovalMode.BIDIRECTIONAL;
		} else {
			logger.warn("Unknown subscrition approval mode: {}. Using default configuration value: bidirectional. Allowed values: 'bidirectional', 'unidirectional'",
					sApprovalMode);
		}
	}

}
