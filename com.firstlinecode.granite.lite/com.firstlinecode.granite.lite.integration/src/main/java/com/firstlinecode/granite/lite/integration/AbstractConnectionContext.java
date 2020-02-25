package com.firstlinecode.granite.lite.integration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.framework.core.session.ValueWrapper;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

public abstract class AbstractConnectionContext implements IConnectionContext {
	private static final Logger logger = LoggerFactory.getLogger(AbstractConnectionContext.class);
	
	protected IMessageChannel messageChannel;
	protected ISession session;
	
	public AbstractConnectionContext(ISession session, IMessageChannel messageChannel) {
		this.session = session;
		this.messageChannel = messageChannel;
	}

	@Override
	public <T> T setAttribute(Object key, T value) {
		return session.setAttribute(key, value);
	}

	@Override
	public <T> T getAttribute(Object key) {
		return session.getAttribute(key);
	}

	@Override
	public <T> T getAttribute(Object key, T defaultValue) {
		return session.getAttribute(key, defaultValue);
	}

	@Override
	public <T> T removeAttribute(Object key) {
		return session.removeAttribute(key);
	}
	
	@Override
	public <T> T setAttribute(Object key, ValueWrapper<T> wrapper) {
		return session.setAttribute(key, wrapper);
	}

	@Override
	public Object[] getAttributeKeys() {
		return session.getAttributeKeys();
	}

	@Override
	public JabberId getJid() {
		return session.getJid();
	}
	
	@Override
	public void write(Object message) {
		if (isAcceptedType(message.getClass())) {
			messageChannel.send(createMessage(message));
		} else {
			logger.warn("Unaccepted type: {}.", message.getClass());
		}
	}
	
	protected IMessage createMessage(Object message) {
		Map<Object, Object> header = new HashMap<>();
		header.put(IMessage.KEY_SESSION_JID, session.getJid());
		
		return new SimpleMessage(header, message);
	}
	
	@Override
	public void close() {
		Map<Object, Object> header = new HashMap<>();
		header.put(IMessage.KEY_SESSION_JID, session.getJid());
		
		messageChannel.send(new SimpleMessage(header, getStreamCloseMessage()));
	}
	
	public static class MessageOutConnectionContext extends AbstractConnectionContext {
		public MessageOutConnectionContext(ISession session, IMessageChannel messageChannel) {
			super(session, messageChannel);
		}

		@Override
		protected Object getStreamCloseMessage() {
			Map<Object, Object> header = new HashMap<>();
			header.put(IMessage.KEY_SESSION_JID, session.getJid());
			header.put(IMessage.KEY_MESSAGE_TARGET, session.getJid());
			
			return new SimpleMessage(header, new Stream(true));
		}

		@Override
		protected boolean isAcceptedType(Class<?> type) {
			return (IMessage.class.isAssignableFrom(type));
		}
		
		@Override
		protected IMessage createMessage(Object message) {
			return (IMessage)message;
		}
	}

	public static class ProcessingContext extends ObjectOutConnectionContext implements IProcessingContext {

		public ProcessingContext(ISession session, IMessageChannel messageChannel) {
			super(session, messageChannel);
		}
		
		protected IMessage createMessage(JabberId target, Object message) {
			Map<Object, Object> header = new HashMap<>();
			header.put(IMessage.KEY_SESSION_JID, session.getJid());
			header.put(IMessage.KEY_MESSAGE_TARGET, target);
			
			return new SimpleMessage(header, message);
		}

		@Override
		public void write(JabberId target, Object message) {
			messageChannel.send(createMessage(target, message));
		}
		
		@Override
		protected boolean isAcceptedType(Class<?> type) {
			return (Stanza.class.isAssignableFrom(type)) ||
					(IError.class.isAssignableFrom(type)) ||
					(Stream.class == type) ||
					IMessage.class.isAssignableFrom(type);
		}
		
	}

	public static class StringOutConnectionContext extends AbstractConnectionContext {
		private static final String STREAM_CLOSE_MESSAGE = OxmService.createMinimumOxmFactory().translate(new Stream(true));

		public StringOutConnectionContext(ISession session, IMessageChannel messageChannel) {
			super(session, messageChannel);
		}

		@Override
		protected Object getStreamCloseMessage() {
			return STREAM_CLOSE_MESSAGE;
		}

		@Override
		protected boolean isAcceptedType(Class<?> type) {
			return type == String.class;
		}
		
	}

	public static class ObjectOutConnectionContext extends AbstractConnectionContext {
		protected static final Stream STREAM_CLOSE_MESSAGE = new Stream(true);

		public ObjectOutConnectionContext(ISession session, IMessageChannel messageChannel) {
			super(session, messageChannel);
		}

		@Override
		protected Object getStreamCloseMessage() {
			return STREAM_CLOSE_MESSAGE;
		}

		@Override
		protected boolean isAcceptedType(Class<?> type) {
			return (Stanza.class.isAssignableFrom(type)) ||
					(IError.class.isAssignableFrom(type)) ||
					(Stream.class == type);
		}
		
	}

	protected abstract Object getStreamCloseMessage();
	protected abstract boolean isAcceptedType(Class<?> type);
	
}

