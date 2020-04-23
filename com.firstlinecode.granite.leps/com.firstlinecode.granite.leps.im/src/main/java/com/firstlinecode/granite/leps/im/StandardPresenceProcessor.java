package com.firstlinecode.granite.leps.im;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.error.FeatureNotImplemented;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAllowed;
import com.firstlinecode.basalt.protocol.core.stanza.error.UnexpectedRequest;
import com.firstlinecode.basalt.protocol.core.stream.error.InternalServerError;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.protocol.im.stanza.Presence.Type;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.commons.utils.StanzaCloner;
import com.firstlinecode.granite.framework.core.event.IEventProducer;
import com.firstlinecode.granite.framework.core.event.IEventProducerAware;
import com.firstlinecode.granite.framework.im.IResource;
import com.firstlinecode.granite.framework.im.IResourcesRegister;
import com.firstlinecode.granite.framework.im.IResourcesService;
import com.firstlinecode.granite.framework.im.ISubscriptionService;
import com.firstlinecode.granite.framework.im.ResourceAvailabledEvent;
import com.firstlinecode.granite.framework.im.Subscription;
import com.firstlinecode.granite.framework.im.Subscription.State;
import com.firstlinecode.granite.framework.processing.IPresenceProcessor;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

public class StandardPresenceProcessor implements IPresenceProcessor, IEventProducerAware {
	@Dependency("authenticator")
	private IAuthenticator authenticator;
	
	@Dependency("resources.service")
	private IResourcesService resourcesService;
	
	@Dependency("resources.register")
	private IResourcesRegister resourcesRegister;
	
	@Dependency("subscription.service")
	private ISubscriptionService subscriptionService;
	
	private IEventProducer eventProducer;

	@Override
	public boolean process(IProcessingContext context, Presence presence) {
		if (presence.getType() != null &&
				presence.getType() != Type.UNAVAILABLE &&
					presence.getType() != Type.PROBE)
			return false;
		
		JabberId to = presence.getTo();
		if (to != null && !isInstantMessagingUser(to)) {
			return false;
		}
		
		JabberId from = presence.getFrom();
		if (from == null) {
			presence.setFrom(context.getJid());
		}
		
		if (!presence.getFrom().equals(context.getJid())) {
			throw new ProtocolException(new NotAllowed(String.format("'from' attribute should be %s.", context.getJid())));
		}
		
		return doProcess(context, presence);
	}

	protected boolean doProcess(IProcessingContext context, Presence presence) {
		JabberId user = context.getJid();
		
		IResource resource = resourcesService.getResource(user);
		if (resource == null) {
			throw new ProtocolException(new InternalServerError(String.format("Resource %s doesn't exist.", user)));
		}
		
		if (!resource.isAvailable()) {
			if (presence.getTo() != null || presence.getType() != null) {
				throw new ProtocolException(new UnexpectedRequest("Expect a initial presence."));
			}
			
			// process initial presence
			processInitialPresence(context, user, presence);
			return true;
		}
		
		if (isBroadcastPresence(user, presence)) {
			processBroadcastPresence(context, user, presence);
		} else if (isProbePresence(presence)) {
			processProbePresence(context, presence);
		} else if (isDirectedPresence(presence)) {
			processDirectedPresence(context, presence);
		} else {
			return false;
		}
		
		return true;
	}
	
	private void processInitialPresence(IProcessingContext context, JabberId user, Presence presence) {
		try {
			resourcesRegister.setAvailable(user);
		} catch (Exception e) {
			throw new ProtocolException(new InternalServerError("Can't set resource to be availabled.", e));
		}
		
		try {
			resourcesRegister.setBroadcastPresence(user, presence);
		} catch (Exception e) {
			throw new ProtocolException(new InternalServerError("Can't set resource's initial presence.", e));
		}
		
		if (presence.getTo() == null && presence.getType() == null && presence.getId() != null) {
			// confirm that the initial presence has been received
			Presence confirm = new Presence();
			confirm.setId(presence.getId());
			
			context.write(confirm);
		}
		
		eventProducer.fire(new ResourceAvailabledEvent(user));
		
		List<Subscription> subscriptions = subscriptionService.get(user.getName());
		for (Subscription subscription : subscriptions) {
			boolean toState = isToState(subscription);
			boolean fromState = isFromState(subscription);
			
			if (!toState && !fromState)
				continue;
			
			JabberId contact = JabberId.parse(subscription.getContact());
			IResource[] contactResources = resourcesService.getResources(contact);
			
			if (toState) {
				probeContact(context, user, contactResources);
			}
			
			if (fromState) {
				sendPresenceToContact(context, presence, user, contactResources);
			}
		}
		
		sendPresenceToUserOtherAvailableResources(context, user, presence);
		probeUserOtherAvailableResources(context, user);
	}

	private void probeUserOtherAvailableResources(IProcessingContext context, JabberId user) {
		IResource[] userResources = resourcesService.getResources(user);
		
		for (IResource resource : userResources) {
			if (resource.getJid().equals(user))
				continue;
			
			if (!resource.isAvailable())
				continue;
			
			Presence availiability = StanzaCloner.clone(resource.getBroadcastPresence());
			
			availiability.setFrom(resource.getJid());
			availiability.setTo(user);
			
			context.write(availiability);					
		}
	}

	private void sendPresenceToContact(IProcessingContext context, Presence presence, JabberId user,
				IResource[] contactResources) {
		for (IResource resource : contactResources) {
			if (!resource.isAvailable())
				continue;
			
			Presence availiability = StanzaCloner.clone(presence);
			
			availiability.setFrom(user);
			availiability.setTo(resource.getJid());
			
			context.write(availiability);					
		}
	}

	private void sendPresenceToUserOtherAvailableResources(IProcessingContext context,
				JabberId user, Presence presence) {
		IResource[] userResources = resourcesService.getResources(user);
		
		for (IResource resource : userResources) {
			if (resource.getJid().equals(user))
				continue;
			
			if (!resource.isAvailable())
				continue;
			
			Presence availiability = StanzaCloner.clone(presence);
			
			availiability.setFrom(user);
			availiability.setTo(resource.getJid());
			
			context.write(availiability);					
		}
	}

	private void probeContact(IProcessingContext context, JabberId user,
			IResource[] contactResources) {
		for (IResource resource : contactResources) {
			if (!resource.isAvailable())
				continue;
			
			Presence availiability = StanzaCloner.clone(resource.getBroadcastPresence());
			
			availiability.setFrom(resource.getJid());
			availiability.setTo(user);
			
			context.write(availiability);
		}
	}
	
	private boolean isFromState(Subscription subscription) {
		State state = subscription.getState();
		return state == State.FROM || state == State.FROM_PENDING_OUT || state == State.BOTH;
	}

	private boolean isToState(Subscription subscription) {
		State state = subscription.getState();		
		return state == State.TO || state == State.TO_PENDING_IN || state == State.BOTH;
	}

	private boolean isBroadcastPresence(JabberId user, Presence presence) {
		if (presence.getTo() != null ||
				(presence.getType() != Presence.Type.UNAVAILABLE &&
					presence.getType() != null))
			return false;
		
		return true;
	}
	
	private void processBroadcastPresence(IProcessingContext context, JabberId user, Presence presence) {
		try {
			resourcesRegister.setBroadcastPresence(user, presence);
		} catch (Exception e) {
			throw new ProtocolException(new InternalServerError("Can't set resource's broadcast presence.", e));
		}
		
		List<Subscription> subscriptions = subscriptionService.get(user.getName());
		for (Subscription subscription : subscriptions) {
			boolean fromState = isFromState(subscription);
			
			if (!fromState)
				continue;
			
			JabberId contact = JabberId.parse(subscription.getContact());
			IResource[] contactResources = resourcesService.getResources(contact);
			
			sendPresenceToContact(context, presence, user, contactResources);
		}
		
		sendPresenceToUserOtherAvailableResources(context, user, presence);
	}
	
	private boolean isProbePresence(Presence presence) {
		return presence.getType() == Presence.Type.PROBE;
	}

	private void processProbePresence(IProcessingContext context, Presence presence) {
		// TODO
		throw new ProtocolException(new FeatureNotImplemented("Feature presence probe isn't implemented yet."));
	}
	
	private boolean isDirectedPresence(Presence presence) {
		return presence.getTo() != null;
	}
	
	private void processDirectedPresence(IProcessingContext context, Presence presence) {
		try {
			resourcesRegister.setDirectedPresence(context.getJid(), presence.getTo(), presence);
		} catch (Exception e) {
			throw new ProtocolException(new InternalServerError("Can't set resource's directed presence.", e));
		};
		
		// Server Rules for Handling XML Stanzas(rfc3920 11)
		if (presence.getTo().getResource() == null) {
			IResource[] resources = resourcesService.getResources(presence.getTo());
			for (IResource resource : resources) {
				if (resource.isAvailable()) {
					context.write(resource.getJid(), presence);
				}
			}
		} else {
			IResource resource = resourcesService.getResource(presence.getTo());
			if (resource.isAvailable()) {
				context.write(presence);
			}
		}
	}

	protected boolean isInstantMessagingUser(JabberId jid) {
		return authenticator.exists(jid.getName());
	}

	@Override
	public void setEventProducer(IEventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}

}
