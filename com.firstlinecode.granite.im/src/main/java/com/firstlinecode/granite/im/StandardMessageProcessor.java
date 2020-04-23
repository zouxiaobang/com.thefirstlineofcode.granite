package com.firstlinecode.granite.im;

import java.util.ArrayList;
import java.util.List;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.event.IEventProducer;
import com.firstlinecode.granite.framework.core.event.IEventProducerAware;
import com.firstlinecode.granite.framework.im.IResource;
import com.firstlinecode.granite.framework.im.IResourcesService;
import com.firstlinecode.granite.framework.im.OfflineMessageEvent;
import com.firstlinecode.granite.framework.processing.IMessageProcessor;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

public class StandardMessageProcessor implements IMessageProcessor, IEventProducerAware, IApplicationConfigurationAware {
	@Dependency("authenticator")
	private IAuthenticator authenticator;
	
	@Dependency("resources.service")
	private IResourcesService resourcesService;
	
	private IEventProducer eventProducer;
	
	private String domain;

	@Override
	public boolean process(IProcessingContext context, Message message) {
		if (message.getType() == Message.Type.GROUPCHAT)
			return false;
		
		return doProcess(context, message);
	}

	protected boolean doProcess(IProcessingContext context, Message message) {
		if (message.getTo() == null) {
			throw new ProtocolException(new BadRequest("A message should specify an intended recipient."));
		}
		
		if (isToSelf(message.getFrom(), message.getTo())) {
			throw new ProtocolException(new BadRequest("Sending a message to yourself."));
		}
		
		if (!isToDomain(message.getTo()) && !authenticator.exists(message.getTo().getName())) {
			return false;
		}
		
		deliverMessage(context, message);
		return true;
	}
	
	private boolean isToDomain(JabberId to) {
		return to.getDomain().equals(domain);
	}
	
	private boolean isToSelf(JabberId from, JabberId to) {
		return from.getBareIdString().equals(to.getBareIdString());
	}
	
	// Server Rules for Handling XML Stanzas(rfc3920 11)
	protected void deliverMessage(IProcessingContext context, Message message) {
		JabberId to = message.getTo();
		if (to.getResource() != null) {
			IResource resoure = resourcesService.getResource(message.getTo());
			
			if (resoure != null && resoure.isAvailable()) {
				context.write(message);
				
				return;
			}
			
			to = to.getBareId();
		}
		
		IResource[] resources = resourcesService.getResources(to);
		if (resources.length == 0) {
			eventProducer.fire(new OfflineMessageEvent(context.getJid(), message.getTo(), message));
		} else {
			IResource[] chosen = chooseTargets(resources);
			if (chosen == null || chosen.length == 0) {
				eventProducer.fire(new OfflineMessageEvent(context.getJid(), message.getTo(), message));
			} else {
				for (IResource resource : chosen) {
					context.write(resource.getJid(), message);
				}
			}
		}
	}

	protected IResource[] chooseTargets(IResource[] resources) {
		List<IResource> lResources = new ArrayList<>();
		
		int priority = 0;
		
		for (IResource resource : resources) {
			if (!resource.isAvailable()) {
				continue;
			}
			
			int resourcePriority = 0;
			if (resource.getBroadcastPresence() != null && resource.getBroadcastPresence().getPriority() != null) {
				resourcePriority = resource.getBroadcastPresence().getPriority();
			}
			
			if(resourcePriority < priority) {
				continue;
			} else if (resourcePriority == priority) {
				lResources.add(resource);
			} else {
				lResources.clear();
				lResources.add(resource);
			}
		}
		
		return lResources.toArray(new IResource[lResources.size()]);
	}

	@Override
	public void setEventProducer(IEventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		this.domain = appConfiguration.getDomainName();
	}
}
