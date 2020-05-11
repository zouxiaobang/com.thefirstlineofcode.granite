package com.firstlinecode.granite.cluster.integration;

import java.util.HashMap;
import java.util.Map;

import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;
import com.firstlinecode.granite.framework.core.session.ISession;

@Component("cluster.stream.2.parsing.message.receiver")
public class Stream2ParsingMessageReceiver extends LocalMessageIntegrator {
	private static final String CONFIGURATION_KEY_STREAM_2_PARSING_MESSAGE_QUEUE_MAX_SIZE = "stream.2.parsing.message.queue.max.size";
	private static final int DEFAULT_MESSAGE_QUEUE_MAX_SIZE = 1024 * 64;
	
	@Override
	protected int getDefaultMessageQueueMaxSize() {
		return DEFAULT_MESSAGE_QUEUE_MAX_SIZE;
	}
	
	@Override
	protected String getMessageQueueMaxSizeConfigurationKey() {
		return CONFIGURATION_KEY_STREAM_2_PARSING_MESSAGE_QUEUE_MAX_SIZE;
	}

	@Override
	protected IConnectionContext doGetConnectionContext(ISession session) {
		return new ParsingConnectionContext(messageChannel, session);
	}
	
	private class ParsingConnectionContext extends AbstractConnectionContext {
		
		public ParsingConnectionContext(IMessageChannel messageChannel, ISession session) {
			super(messageChannel, session);
		}

		@Override
		public void close() {
			Map<Object, Object> headers = new HashMap<>();
			headers.put(IMessage.KEY_SESSION_JID, session.getJid());
			headers.put(IMessage.KEY_MESSAGE_TARGET, session.getJid());
			
			IMessage message = new SimpleMessage(headers, new Stream(true));
			
			messageChannel.send(new SimpleMessage(headers, message));
		}

		@Override
		protected boolean isMessageAccepted(Object message) {
			Class<?> messageType = message.getClass();
			return (Stanza.class.isAssignableFrom(messageType)) ||
					(IError.class.isAssignableFrom(messageType)) ||
					(Stream.class == messageType);
		}
		
	}

	@Override
	protected String getOsgiServicePid() {
		return Constants.STREAM_2_PARSING_MESSAGE_INTEGRATOR;
	}
}
