package com.firstlinecode.granite.framework.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.event.IEvent;
import com.firstlinecode.granite.framework.core.event.IEventContext;
import com.firstlinecode.granite.framework.core.event.IEventListener;
import com.firstlinecode.granite.framework.core.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.pipe.IMessage;
import com.firstlinecode.granite.framework.core.pipe.IMessageProcessor;
import com.firstlinecode.granite.framework.core.pipe.SimpleMessage;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

@Component("default.event.processor")
public class DefaultEventProcessor implements IMessageProcessor, IInitializable,
		IServerConfigurationAware, IApplicationComponentServiceAware {
	private Logger logger = LoggerFactory.getLogger(DefaultEventProcessor.class);
	
	protected Map<Class<? extends IEvent>, List<IEventListener<?>>> eventToListeners;
	
	private IApplicationComponentService appComponentService;
	private JabberId serverJid;
	
	public DefaultEventProcessor() {
		eventToListeners = new HashMap<>();
	}

	@Override
	public void process(IConnectionContext context, IMessage message) {
		try {
			processEvent(context, (IEvent)message.getPayload());
		} catch (Exception e) {
			logger.error("Event processing error.", e);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private <E extends IEvent> void processEvent(IConnectionContext context, E event) {
		List<IEventListener<?>> listeners = eventToListeners.get(event.getClass());
		if (listeners == null || listeners.size() == 0) {
			if (logger.isWarnEnabled()) {
				logger.warn("No event listener is listening to event {}.", event.getClass().getName());
			}
			
			return;
		}
		
		for (IEventListener<?> listener : listeners) {
			((IEventListener<E>)listener).process(getEventContext(context), (E)(event.clone()));
		}
	}

	private IEventContext getEventContext(IConnectionContext context) {
		return new EventContext(context);
	}
	
	private class EventContext implements IEventContext {
		private IConnectionContext connectionContext;
		
		public EventContext(IConnectionContext connectionContext) {
			this.connectionContext = connectionContext;
		}
		
		@Override
		public void write(Stanza stanza) {
			Map<Object, Object> headers = new HashMap<>();
			headers.put(IMessage.KEY_SESSION_JID, serverJid);
			
			connectionContext.write(new SimpleMessage(headers, stanza));
		}

		@Override
		public void write(JabberId target, Stanza stanza) {
			Map<Object, Object> headers = new HashMap<>();
			headers.put(IMessage.KEY_SESSION_JID, serverJid);
			headers.put(IMessage.KEY_MESSAGE_TARGET, target);
			
			connectionContext.write(new SimpleMessage(headers, stanza));
		}

		@Override
		public void write(JabberId target, String message) {
			Map<Object, Object> headers = new HashMap<>();
			headers.put(IMessage.KEY_SESSION_JID, serverJid);
			headers.put(IMessage.KEY_MESSAGE_TARGET, target);
			
			connectionContext.write(new SimpleMessage(headers, message));
		}
	}

	@Override
	public void init() {
		loadContributedEventListeners();
	}
	
	@SuppressWarnings("rawtypes")
	private void loadContributedEventListeners() {
		List<Class<? extends IEventListenerFactory>> listenerFactoryClasses = appComponentService.getExtensionClasses(IEventListenerFactory.class);
		if (listenerFactoryClasses == null || listenerFactoryClasses.size() == 0) {
			if (logger.isDebugEnabled())
				logger.debug("No extension which's extension point is {} found.", IEventListenerFactory.class.getName());
			
			return;
		}
		
		for (Class<? extends IEventListenerFactory>listenerFactoryClass : listenerFactoryClasses) {
			IEventListenerFactory<?> listenerFactory = appComponentService.createExtension(listenerFactoryClass);
			List<IEventListener<?>> listeners = eventToListeners.get(listenerFactory.getType());
			if (listeners == null)
				listeners = new ArrayList<>();
			
			listeners.add(listenerFactory.createListener());
			eventToListeners.put(listenerFactory.getType(), listeners);
		}
	}
	
	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		serverJid = JabberId.parse(serverConfiguration.getDomainName());
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}
}
