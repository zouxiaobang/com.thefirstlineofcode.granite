package com.firstlinecode.granite.framework.im;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.event.ConnectionClosedEvent;
import com.firstlinecode.granite.framework.core.event.IEventContext;
import com.firstlinecode.granite.framework.core.event.IEventListener;

public class ConnectionClosedListener implements IEventListener<ConnectionClosedEvent> {
	@Dependency("resources.register")
	private IResourcesRegister register;
	
	@Dependency("resources.service")
	private IResourcesService resourcesService;
	
	@Dependency("subscription.service")
	private ISubscriptionService subscriptionService;

	@Override
	public void process(IEventContext context, ConnectionClosedEvent event) {
		JabberId user = event.getJid();
		
		IResource resource = resourcesService.getResource(user);
		
		Presence broadcastPresence = null;
		if (resource != null) {
			broadcastPresence = resource.getBroadcastPresence();
		}
		
		if (resource == null)
			return;
		
		if (broadcastPresence != null && broadcastPresence.getType() != Presence.Type.UNAVAILABLE) {
			List<Subscription> subscriptions = subscriptionService.get(user.getNode());
			for (Subscription subscription : subscriptions) {
				boolean fromState = isFromState(subscription);
				
				if (!fromState)
					continue;
				
				sendUnavailableToContact(context, user, JabberId.parse(subscription.getContact()));
			}
			
			sendUnavailableToUserOtherAvailableResources(context, user);
		}
	}
	
	private void sendUnavailableToUserOtherAvailableResources(IEventContext context, JabberId user) {
		IResource[] userResources = resourcesService.getResources(user);
		
		for (IResource resource : userResources) {
			if (resource.getJid().equals(user))
				continue;
			
			if (!resource.isAvailable())
				continue;
			
			Presence directedPresence = resource.getDirectedPresence(user);
			if (directedPresence != null && directedPresence.getType() == Presence.Type.UNAVAILABLE)
				continue;
			
			Presence unavailable = new Presence();
			
			unavailable.setType(Presence.Type.UNAVAILABLE);
			unavailable.setFrom(user);
			unavailable.setTo(resource.getJid());
			
			context.write(unavailable);					
		}
	}
	
	private boolean isFromState(Subscription subscription) {
		Subscription.State state = subscription.getState();
		return state == Subscription.State.FROM ||
				state == Subscription.State.FROM_PENDING_OUT ||
					state == Subscription.State.BOTH;
	}
	
	private void sendUnavailableToContact(IEventContext context, JabberId user, JabberId contact) {
		IResource[] contactResources = resourcesService.getResources(contact);
		
		for (IResource resource : contactResources) {
			if (!resource.isAvailable())
				continue;
			
			Presence directedPresence = resource.getDirectedPresence(user);
			if (directedPresence != null && directedPresence.getType() == Presence.Type.UNAVAILABLE)
				continue;
			
			Presence unavailable = new Presence();
			
			unavailable.setType(Presence.Type.UNAVAILABLE);
			unavailable.setFrom(user);
			unavailable.setTo(resource.getJid());
			
			context.write(unavailable);					
		}
	}
}

