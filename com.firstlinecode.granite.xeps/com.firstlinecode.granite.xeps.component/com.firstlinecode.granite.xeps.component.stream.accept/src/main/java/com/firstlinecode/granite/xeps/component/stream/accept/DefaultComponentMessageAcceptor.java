package com.firstlinecode.granite.xeps.component.stream.accept;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.InvalidXml;
import com.firstlinecode.basalt.protocol.oxm.IOxmFactory;
import com.firstlinecode.basalt.protocol.oxm.OxmService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManagerAware;
import com.firstlinecode.granite.framework.core.integration.IMessageProcessor;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.stream.standard.SocketConnectionContext;
import com.firstlinecode.granite.stream.standard.codec.MessageDecoder;
import com.firstlinecode.granite.stream.standard.codec.MessageEncoder;
import com.firstlinecode.granite.xeps.component.stream.IComponentConnectionsRegister;
import com.firstlinecode.granite.xeps.component.stream.IComponentMessageAcceptor;
import com.firstlinecode.granite.xeps.component.stream.IComponentMessageProcessor;

@Component("default.component.message.acceptor")
public class DefaultComponentMessageAcceptor extends IoHandlerAdapter
		implements IComponentMessageAcceptor, IConfigurationAware {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultComponentMessageAcceptor.class);
	
	private static final String CONFIGURATION_KEY_PORT = "port";
	private static final String CONFIGURATION_KEY_CONNECTION_TIMEOUT = "connection.timeout";
	private static final String CONFIGURATION_KEY_NUMBER_OF_PROCESSORS = "number.of.processors";
	
	private int port;
	private int connectionTimeout;
	private int numberOfProcessors;
	
	private NioSocketAcceptor acceptor;
	
	private IMessageProcessor messageProcessor;
	
	private static final IOxmFactory oxmFactory = OxmService.createMinimumOxmFactory();
	
	private static final String STRING_INVALID_MESSAGE = oxmFactory.translate(new InvalidXml());
	private static final String STRING_BAD_REQUEST = oxmFactory.translate(new BadRequest());
	private static final String STRING_CLOSE_STREAM = oxmFactory.translate(new Stream(true));
	
	private IComponentConnectionsRegister connectionsRegister;
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		port = configuration.getInteger(CONFIGURATION_KEY_PORT, 8222);
		connectionTimeout = configuration.getInteger(CONFIGURATION_KEY_CONNECTION_TIMEOUT, 5 * 60);
		numberOfProcessors = configuration.getInteger(CONFIGURATION_KEY_NUMBER_OF_PROCESSORS,
				Runtime.getRuntime().availableProcessors());
	}
	
	@Override
	public synchronized void start() throws Exception {
		((IComponentMessageProcessor)messageProcessor).setComponentConnectionsRegister(connectionsRegister);
		if (messageProcessor instanceof IConnectionManagerAware) {
			((IConnectionManagerAware)messageProcessor).setConnectionManager(this);
		}
		
		bindSocketAcceptor();
		
		logger.info("default component message acceptor has started");
	}
	
	private void bindSocketAcceptor() throws IOException {
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new SimpleBufferAllocator());
		acceptor = new NioSocketAcceptor(numberOfProcessors);
		
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(MessageEncoder.class, MessageDecoder.class));
		acceptor.getFilterChain().addLast("exceutor", new ExecutorFilter(16, numberOfProcessors * 16));
		
		acceptor.setHandler(this);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, connectionTimeout);
		
		acceptor.setCloseOnDeactivation(false);
		acceptor.bind(new InetSocketAddress(port));
		
		logger.info("default component message acceptor has binded on port {}.", port);
	}

	@Override
	public synchronized void stop() throws Exception {
		if (acceptor != null) {
			acceptor.unbind();
			
			for (long clientSessionId : acceptor.getManagedSessions().keySet()) {
				connectionsRegister.unregister(clientSessionId);
			}
			
			acceptor.dispose();
		}
		
		logger.info("default component message acceptor has stopped");
	}

	@Override
	public synchronized boolean isActive() {
		if (acceptor != null) {
			return acceptor.isActive();
		}
		
		return false;
	}
	
	@Dependency("message.processor")
	@Override
	public void setMessageProcessor(IMessageProcessor messageProcessor) {
		if (!(messageProcessor instanceof IComponentMessageProcessor)) {
			throw new IllegalArgumentException(String.format("%s must implement interface %s",
					messageProcessor.getClass().getName(), IComponentMessageProcessor.class.getName()));
			
		}
		
		this.messageProcessor = messageProcessor;
	}
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("session opened[{}]", session);
		}
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("session closed[{}]", session);
		}
		
		connectionsRegister.unregister(session.getId());
	}
	
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("session idle[{}, {}]", session, status);
		}
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("message received[{}, {}]", session, message);
		}
		
		messageProcessor.process(new SocketConnectionContext(session, null), new SimpleMessage(message));
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("message sent[{}, {}]", session, message);
		}
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		if (cause instanceof ProtocolDecoderException) { // the exception is thrown by message decoder
			if (logger.isDebugEnabled()) {
				logger.debug("protocol decoder exception caught", cause);
			}
			
			if (session.getAttribute(ISession.KEY_SESSION_JID) != null) {
				session.write(STRING_BAD_REQUEST);
			} else {
				session.write(STRING_INVALID_MESSAGE);
			}
			
			session.write(STRING_CLOSE_STREAM);
			session.close(true);

		} else {
			if (logger.isWarnEnabled()) {
				logger.warn("exception caught", cause);
			}
		}
	}

	@Override
	public IConnectionContext getConnectionContext(JabberId sessionJid) {
		Object clientSessionId = connectionsRegister.getConnectionId(sessionJid.getDomain());
		
		if (clientSessionId == null) {
			logger.warn("null client session id. session jid is {}", sessionJid);		
			return null;
		}
		
		IoSession clientSession = acceptor.getManagedSessions().get(clientSessionId);
		if (clientSession != null) {
			return new SocketConnectionContext(clientSession, null);
		}
		
		return null;
	}

	@Override
	public void setComponentConnectionsRegister(IComponentConnectionsRegister connectionsRegister) {
		this.connectionsRegister = connectionsRegister;
	}

}
