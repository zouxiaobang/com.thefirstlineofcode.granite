package com.firstlinecode.granite.leps.im.roster;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stream.error.InternalServerError;
import com.firstlinecode.basalt.protocol.im.roster.Item;
import com.firstlinecode.basalt.protocol.im.roster.Item.Ask;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.granite.framework.core.annotations.AppComponent;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.supports.data.IDataObjectFactory;
import com.firstlinecode.granite.framework.core.supports.data.IDataObjectFactoryAware;
import com.firstlinecode.granite.framework.im.IResource;
import com.firstlinecode.granite.framework.im.IResourcesRegister;
import com.firstlinecode.granite.framework.im.IResourcesService;
import com.firstlinecode.granite.framework.im.ISubscriptionService;
import com.firstlinecode.granite.framework.im.ResourceRegistrationException;
import com.firstlinecode.granite.framework.im.Subscription;
import com.firstlinecode.granite.framework.im.Subscription.State;
import com.firstlinecode.granite.framework.processing.ProcessingUtils;

@AppComponent("roster.operator")
public class RosterOperator implements IApplicationConfigurationAware, IDataObjectFactoryAware {
	
	@Dependency("subscription.service")
	private ISubscriptionService subscriptionService;
	
	@Dependency("resources.service")
	private IResourcesService resourcesService;
	
	@Dependency("resources.register")
	private IResourcesRegister resourcesRegister;
	
	private IDataObjectFactory dataObjectFactory;
	
	private String domain;
	
	public void reply(IConnectionContext context, JabberId userJid, String id) {
		context.write(ProcessingUtils.createIqResult(userJid, id));
	}

	private void rosterPush(IConnectionContext context, String user, String contact) {
		rosterPush(context, user, getRoster(user, contact));
	}
	
	public void rosterPush(IConnectionContext context, String user, Roster roster) {
		IResource[] resources = resourcesService.getResources(
				JabberId.parse(new StringBuilder().
						append(user).
						append("@").
						append(domain).
						toString()
				)
			);
		
		if (resources.length == 0)
			return;
		
		for (IResource resource : resources) {
			if(!resource.isRosterRequested())
				continue;
			
			Iq iq = new Iq();
			iq.setType(Iq.Type.SET);
			iq.setTo(resource.getJid());
			iq.setObject(roster);
			
			context.write(iq);
		}
	}

	private Roster getRoster(String user, String contact) {
		return subscriptionToRoster(subscriptionService.get(user, contact));
	}

	public Roster subscriptionToRoster(Subscription subscription) {
		Item item = new Item();
		item.setJid(JabberId.parse(subscription.getContact()));
		item.setName(subscription.getName());
		item.setGroups(groupStringToList(subscription.getGroups()));
		
		subscriptionStateToItemState(item, subscription);
		
		Roster roster = new Roster();
		roster.addOrUpdate(item);
		return roster;
	}
	
	void subscriptionStateToItemState(Item item, Subscription subscription) {
		if (subscription.getState() == State.NONE) {
			item.setSubscription(Item.Subscription.NONE);
		} else if (subscription.getState() == State.FROM) {
			item.setSubscription(Item.Subscription.FROM);
		} else if (subscription.getState() == State.TO) {
			item.setSubscription(Item.Subscription.TO);
		} else if (subscription.getState() == State.BOTH) {
			item.setSubscription(Item.Subscription.BOTH);
		} else if (subscription.getState() == State.NONE_PENDING_IN) {
			item.setSubscription(Item.Subscription.NONE);
		} else if (subscription.getState() == State.NONE_PENDING_OUT) {
			item.setSubscription(Item.Subscription.NONE);
			item.setAsk(Ask.SUBSCRIBE);
		} else if (subscription.getState() == State.NONE_PENDING_IN_OUT) {
			item.setSubscription(Item.Subscription.NONE);
			item.setAsk(Ask.SUBSCRIBE);
		} else if (subscription.getState() == State.FROM_PENDING_OUT) {
			item.setSubscription(Item.Subscription.FROM);
			item.setAsk(Ask.SUBSCRIBE);
		} else { // subscription.getState() == State.TO_PENDING_IN
			item.setSubscription(Item.Subscription.TO);
		}
	}

	List<String> groupStringToList(String sGroups) {
		if (sGroups == null)
			return new ArrayList<>();
		
		StringTokenizer st = new StringTokenizer(sGroups, ",");
		List<String> groups = new ArrayList<>();
		while (st.hasMoreTokens()) {
			groups.add(st.nextToken());
		}
		
		return groups;
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		domain = appConfiguration.getDomainName();
	}
	
	public void rosterSet(IConnectionContext context, JabberId userJid, Roster roster) {
		if (roster.getItems().length != 1) {
			throw new ProtocolException(new BadRequest("only one item allowed in roster set"));
		}
		
		Item item = roster.getItems()[0];
		String contact = item.getJid().getBareIdString();
		
		if (!subscriptionService.exists(userJid.getName(), contact)) {
			rosterAdd(userJid.getName(), contact, item.getName(), item.getGroups());
		} else {
			rosterUpdate(userJid.getName(), contact, item.getName(), item.getGroups());
		}
		
		rosterPush(context, userJid.getName(), contact);
	}
	
	private void rosterAdd(String user, String contact, String nickname, List<String> groups) {
		Subscription subscription = dataObjectFactory.create(Subscription.class);
		subscription.setUser(user);
		subscription.setContact(contact);
		subscription.setName(nickname);
		subscription.setState(Subscription.State.NONE);
		subscription.setGroups(groupListToString(groups));
		
		subscriptionService.add(subscription);
	}
	
	private String groupListToString(List<String> groups) {
		if (groups == null || groups.size() == 0)
			return null;
		
		StringBuilder sb = new StringBuilder();
		
		for (String group : groups) {
			sb.append(group).append(',');
		}
		
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}
	
	public void rosterGet(IConnectionContext context, JabberId userJid, String requestId) {
		List<Subscription> subscriptions = subscriptionService.get(userJid.getName());
		
		Roster roster = new Roster();
		for (Subscription subscription : subscriptions) {
			Item item = new Item();
			item.setJid(JabberId.parse(subscription.getContact()));
			item.setName(subscription.getName());
			subscriptionStateToItemState(item, subscription);
			item.setGroups(groupStringToList(subscription.getGroups()));
			
			roster.addOrUpdate(item);
		}
		
		Iq iq = new Iq();
		iq.setType(Iq.Type.RESULT);
		iq.setId(requestId);
		iq.setObject(roster);
		
		context.write(iq);
		
		try {
			resourcesRegister.setRosterRequested(userJid);
		} catch (ResourceRegistrationException e) {
			throw new ProtocolException(new InternalServerError("Can't set resource's roster to be requested."), e);
		}
	}
	
	private void rosterUpdate(String user, String contact, String nickname, List<String> groups) {
		subscriptionService.updateNameAndGroups(user, contact, nickname, groupListToString(groups));
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}
}
