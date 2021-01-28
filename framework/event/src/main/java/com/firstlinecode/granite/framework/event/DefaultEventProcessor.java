package com.firstlinecode.granite.framework.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.IContributionTracker;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.commons.utils.CommonUtils;
import com.firstlinecode.granite.framework.core.commons.utils.IOrder;
import com.firstlinecode.granite.framework.core.commons.utils.OrderComparator;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.event.IEvent;
import com.firstlinecode.granite.framework.core.event.IEventContext;
import com.firstlinecode.granite.framework.core.event.IEventListener;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageProcessor;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;
import com.firstlinecode.granite.framework.core.supports.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

@Component("default.event.processor")
public class DefaultEventProcessor implements IMessageProcessor, IBundleContextAware,
		IInitializable, IApplicationConfigurationAware {
	private Logger logger = LoggerFactory.getLogger(DefaultEventProcessor.class);
	
	private static final String SEPARATOR_COMPONENTS = ",";
	private static final String KEY_GRANITE_EVENT_LISTENERS = "Granite-Event-Listeners";
	private static final String PROPERTY_NAME_EVENT = "event";
	private static final String PROPERTY_NAME_EVENT_LISTENER = "event-listener";
	
	protected BundleContext bundleContext;
	
	protected Map<String, IEventListener<?>> eventListeners;
	protected Map<Class<? extends IEvent>, List<OrderId>> eventToListenerIds;
	protected Map<Bundle, List<OrderId>> bundleToListenerIds;
	
	private IContributionTracker eventListenersTracker;
	
	private IApplicationComponentService appComponentService;
	
	private JabberId serverJid;
	
	public DefaultEventProcessor() {
		eventListeners = new ConcurrentHashMap<>();
		eventToListenerIds = new ConcurrentHashMap<>();
		bundleToListenerIds = new ConcurrentHashMap<>();
		
		eventListenersTracker = new EventListenersContributionTracker();
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
	private <E extends IEvent> void processEvent(IConnectionContext context, IEvent event) {
		List<OrderId> ids = eventToListenerIds.get(event.getClass());
		if (ids == null || ids.size() == 0)
			return;
		
		for (OrderId id : ids) {
			IEventListener<E> listener = (IEventListener<E>)eventListeners.get(id.getId());
			if (listener != null)
				listener.process(getEventContext(context), (E)event);
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
		OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_EVENT_LISTENERS, eventListenersTracker);
	}
	
	private class EventListenersContributionTracker implements IContributionTracker {

		@SuppressWarnings("unchecked")
		@Override
		public synchronized void found(Bundle bundle, String contribution) throws Exception {
			StringTokenizer tokenizer = new StringTokenizer(contribution, SEPARATOR_COMPONENTS);
			
			List<OrderId> listenerIds = new ArrayList<>();
			int idSequence = 0;
			while (tokenizer.hasMoreTokens()) {
				String listenerString = tokenizer.nextToken();
				
				Map<String, String> properties = CommonUtils.parsePropertiesString(listenerString,
						new String[] {PROPERTY_NAME_EVENT, PROPERTY_NAME_EVENT_LISTENER});
				String uniqueIdString = bundle.getSymbolicName() + "_" + idSequence;
				idSequence++;
				String sEventClass = properties.get(PROPERTY_NAME_EVENT);
				if (sEventClass == null) {
					throw new IllegalArgumentException("Null event class[register event listener].");
				}
				
				String sEventListenerClass = properties.get(PROPERTY_NAME_EVENT_LISTENER);
				if (sEventListenerClass == null) {
					throw new IllegalArgumentException("Null event listener class[register event listener].");
				}
				
				Class<?> eventClass = bundle.loadClass(sEventClass);
				
				if (!(IEvent.class.isAssignableFrom(eventClass))) {
					throw new IllegalArgumentException(String.format("%s must implement %s[register event listener].",
							sEventClass, IEvent.class));
				}
				
				Class<?> eventListenerClass = bundle.loadClass(sEventListenerClass);
				
				if (!(IEventListener.class.isAssignableFrom(eventListenerClass))) {
					throw new IllegalArgumentException(String.format("%s must implement %s[register event listener].",
							sEventListenerClass, IEventListener.class));
				}
				
				IEventListener<?> eventListener;
				try {
					eventListener = (IEventListener<?>)eventListenerClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("Can't instantiate event listener.", e);
				}
				
				appComponentService.inject(eventListener, bundle.getBundleContext());
				
				OrderId id = new OrderId(uniqueIdString, OrderComparator.getAcceptableOrder(eventListener));
				eventListeners.put(uniqueIdString, eventListener);
				List<OrderId> idsListenToEvent = eventToListenerIds.get(eventClass);
				if (idsListenToEvent == null) {
					idsListenToEvent = new ArrayList<>();
					eventToListenerIds.put((Class<? extends IEvent>)eventClass, idsListenToEvent);
				}
				
				idsListenToEvent.add(id);
			}
			
			bundleToListenerIds.put(bundle, listenerIds);
		}

		@Override
		public synchronized void lost(Bundle bundle, String contribution) throws Exception {
			List<OrderId> listenerIds = bundleToListenerIds.remove(bundle);
			for (List<OrderId> idsListenToEvent : eventToListenerIds.values()) {
				for (OrderId id : listenerIds) {
					if (idsListenToEvent.contains(id)) {
						idsListenToEvent.remove(id);
					}
				}
			}
			
			for (OrderId id : listenerIds) {
				eventListeners.remove(id.getId());
			}
		}
		
	}
	
	private class OrderId implements IOrder {
		private String id;
		private int order;
		
		public OrderId(String id, int order) {
			this.id = id;
			this.order = order;
		}
		
		public String getId() {
			return id;
		}
		
		@Override
		public int getOrder() {
			return order;
		}
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		serverJid = JabberId.parse(appConfiguration.getDomainName());
	}
}
