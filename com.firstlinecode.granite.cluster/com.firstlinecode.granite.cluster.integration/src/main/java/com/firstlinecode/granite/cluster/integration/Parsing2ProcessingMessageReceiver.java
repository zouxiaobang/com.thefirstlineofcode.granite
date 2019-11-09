package com.firstlinecode.granite.cluster.integration;

import java.util.HashMap;
import java.util.Map;

import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.granite.cluster.node.commons.deploying.NodeType;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

@Component("cluster.parsing.2.processing.message.receiver")
public class Parsing2ProcessingMessageReceiver extends LocalMessageIntegrator implements IInitializable {
	private static final String CONFIGURATION_KEY_PARSING_2_PROCESSING_MESSAGE_QUEUE_MAX_SIZE = "parsing.2.processing.message.queue.max.size";
	private static final int DEFAULT_MESSAGE_QUEUE_MAX_SIZE = 1024 * 64;
	
	@Dependency("runtime.configuration")
	private RuntimeConfiguration runtimeConfiguration;
	
	@Override
	protected int getDefaultMessageQueueMaxSize() {
		return DEFAULT_MESSAGE_QUEUE_MAX_SIZE;
	}

	@Override
	protected String getMessageQueueMaxSizeConfigurationKey() {
		return CONFIGURATION_KEY_PARSING_2_PROCESSING_MESSAGE_QUEUE_MAX_SIZE;
	}

	@Override
	protected String getOsgiServicePid() {
		return Constants.PARSING_2_PROCESSING_MESSAGE_INTEGRATOR;
	}

	@Override
	protected IConnectionContext doGetConnectionContext(ISession session) {
		return new ProcessingConnectionContext(messageChannel, session);
	}
	
	private class ProcessingConnectionContext extends AbstractConnectionContext implements IProcessingContext {
		
		public ProcessingConnectionContext(IMessageChannel messageChannel, ISession session) {
			super(messageChannel, session);
		}

		@Override
		public void close() {
			Map<Object, Object> header = new HashMap<>();
			header.put(IMessage.KEY_SESSION_JID, session.getJid());
			header.put(IMessage.KEY_MESSAGE_TARGET, session.getJid());
			
			IMessage message = new SimpleMessage(header, new Stream(true));
			
			messageChannel.send(new SimpleMessage(header, message));
		}

		@Override
		protected boolean isMessageAccepted(Object message) {
			Class<?> messageType = message.getClass();
			return (Stanza.class.isAssignableFrom(messageType)) ||
					(IError.class.isAssignableFrom(messageType)) ||
					(Stream.class == messageType) ||
					(IMessage.class.isAssignableFrom(messageType));
		}
		
		private IMessage createMessage(JabberId target, Object message) {
			Map<Object, Object> header = new HashMap<>();
			header.put(IMessage.KEY_SESSION_JID, session.getJid());
			header.put(IMessage.KEY_MESSAGE_TARGET, target);
			
			return new SimpleMessage(header, message);
		}

		@Override
		public void write(JabberId target, Object message) {
			messageChannel.send(createMessage(target, message));
		}
		
	}

	@Override
	public void init() {
		NodeType nodeType = runtimeConfiguration.getDeployConfiguration().getNodeTypes().get(runtimeConfiguration.getNodeType());
		if (!nodeType.hasAbility("stream")) {
			throw new RuntimeException("Splitting application vertically isn't supported yet. Appnode must possess all im abilities(stream, processing, event).");
		}
	}

}
