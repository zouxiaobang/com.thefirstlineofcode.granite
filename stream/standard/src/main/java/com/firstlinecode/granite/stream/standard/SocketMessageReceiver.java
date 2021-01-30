package com.firstlinecode.granite.stream.standard;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.binary.IBinaryXmppProtocolConverter;
import com.firstlinecode.basalt.oxm.preprocessing.IMessagePreprocessor;
import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.ConnectionTimeout;
import com.firstlinecode.basalt.protocol.core.stream.error.InvalidXml;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.integration.IClientMessageProcessor;
import com.firstlinecode.granite.framework.core.integration.IMessageProcessor;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.routing.ILocalNodeIdProvider;
import com.firstlinecode.granite.framework.core.routing.IRouter;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.framework.core.session.ISessionManager;
import com.firstlinecode.granite.framework.stream.IClientMessageReceiver;
import com.firstlinecode.granite.framework.stream.StreamConstants;
import com.firstlinecode.granite.stream.standard.codec.MessageDecoder;
import com.firstlinecode.granite.stream.standard.codec.MessageEncoder;

@Component("socket.message.receiver")
public class SocketMessageReceiver extends IoHandlerAdapter implements IClientMessageReceiver,
			IApplicationConfigurationAware, IConfigurationAware, IBundleContextAware,
				IInitializable {
	private static final String DIR_NAME_SECURITY = "/security";

	private static final Logger logger = LoggerFactory.getLogger(SocketMessageReceiver.class);
	
	private TlsParameter tlsParameter;
	
	private static final String CONFIGURATION_KEY_IP = "ip";
	private static final String CONFIGURATION_KEY_PORT = "port";
	private static final String CONFIGURATION_KEY_CONNECTION_TIMEOUT = "connection.timeout";
	private static final String CONFIGURATION_KEY_LOGGING_MINA = "logging.mina";
	private static final String CONFIGURATION_KEY_NUMBER_OF_PROCESSORS = "number.of.processors";
	
	private static final IOxmFactory oxmFactory = OxmService.createMinimumOxmFactory();
	
	private static final String STRING_INVALID_MESSAGE = oxmFactory.translate(new InvalidXml());
	private static final String STRING_BAD_REQUEST = oxmFactory.translate(new BadRequest());
	private static final String STRING_CONNECTION_TIMEOUT = oxmFactory.translate(new ConnectionTimeout());
	private static final String STRING_CLOSE_STREAM = oxmFactory.translate(new Stream(true));
	
	private IClientMessageProcessor messageProcessor;
	
	private String ip;
	private int port;
	private int connectionTimeout;
	private boolean loggingMina;
	private int numberOfProcessors;
	
	private String messageFormat;
	
	private NioSocketAcceptor acceptor;
	private ISessionManager sessionManager;
	private IRouter router;
	private ILocalNodeIdProvider localNodeIdProvider;
	
	private BundleContext bundleContext;
	private IBinaryXmppProtocolConverter bxmppProtocolConverter;
	private IMessagePreprocessor binaryMessagePreprocessor;
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		ip = configuration.getString(CONFIGURATION_KEY_IP);
		port = configuration.getInteger(CONFIGURATION_KEY_PORT, getDefaultPort());
		connectionTimeout = configuration.getInteger(CONFIGURATION_KEY_CONNECTION_TIMEOUT, 5 * 60);
		loggingMina = configuration.getBoolean(CONFIGURATION_KEY_LOGGING_MINA, false);
		numberOfProcessors = configuration.getInteger(CONFIGURATION_KEY_NUMBER_OF_PROCESSORS,
				Runtime.getRuntime().availableProcessors());
	}

	protected int getDefaultPort() {
		return 5222;
	}

	@Override
	public synchronized void start() throws Exception {
		bindSocketAcceptor();
		
		logger.info("Socket message receiver has started.");
	}

	protected void bindSocketAcceptor() throws IOException {
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new SimpleBufferAllocator());
		acceptor = new NioSocketAcceptor(numberOfProcessors);
		if (loggingMina) {
			logger.info("Mina protocol events will be logged.");
			
			acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		}
		
		ProtocolEncoder encoder = new MessageEncoder();
		ProtocolDecoder decoder;
		if (bxmppProtocolConverter != null) {
			encoder = new MessageEncoder(bxmppProtocolConverter);
			decoder = new MessageDecoder(binaryMessagePreprocessor);
		} else {
			encoder = new MessageEncoder();
			decoder = new MessageDecoder();
		}
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(encoder, decoder));
		acceptor.getFilterChain().addLast("exceutor", createExecutorFilter());
		
		acceptor.setHandler(this);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, connectionTimeout);
		acceptor.bind(getInetSocketAddress());
		
		logger.info("Socket message receiver has binded on port {}.", port);
	}

	private ExecutorFilter createExecutorFilter() {
		ExecutorFilter executorFilter = new ExecutorFilter();
		OrderedThreadPoolExecutor executor = (OrderedThreadPoolExecutor)executorFilter.getExecutor();
		executor.setMaximumPoolSize(numberOfProcessors * 16);
		executor.setCorePoolSize(16);
		return executorFilter;
	}

	protected InetSocketAddress getInetSocketAddress() {
		return ip == null ? new InetSocketAddress(port) : new InetSocketAddress(ip, port);
	}

	@Override
	public synchronized void stop() throws Exception {
		if (acceptor != null) {
			acceptor.unbind();
			acceptor.dispose();
		}
		
		logger.info("Socket message receiver has stopped.");
	}

	@Override
	public synchronized boolean isActive() {
		if (acceptor != null) {
			return acceptor.isActive();
		}
		
		return false;
	}

	@Override
	@Dependency("message.processor")
	public void setMessageProcessor(IMessageProcessor messageProcessor) {
		if (!(messageProcessor instanceof IClientMessageProcessor)) {
			throw new IllegalArgumentException(String.format("%s should implement %s.",
				messageProcessor.getClass().getName(), IClientMessageProcessor.class.getName()));
		}
		
		this.messageProcessor = (IClientMessageProcessor)messageProcessor;
		this.messageProcessor.setConnectionManager(this);
	}
	
	@Dependency("session.manager")
	public void setSessionManager(ISessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	@Dependency("router")
	public void setRouter(IRouter router) {
		this.router = router;
	}
	
	@Dependency("local.node.id.provider")
	public void setLocalNodeIdProvider(ILocalNodeIdProvider localNodeIdProvider) {
		this.localNodeIdProvider = localNodeIdProvider;
	}
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		session.setAttribute(TlsParameter.KEY_TLS_PARAMETER, tlsParameter);
		if (logger.isDebugEnabled()) {
			logger.debug("Session opened[{}].", session);
		}
		
		messageProcessor.connectionOpened(new SocketConnectionContext(session, localNodeIdProvider.getLocalNodeId()));
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		JabberId sessionJid = (JabberId)session.getAttribute(ISession.KEY_SESSION_JID);
		
		if (sessionJid != null) {
			IClientConnectionContext context = new SocketConnectionContext(session, localNodeIdProvider.getLocalNodeId());
			messageProcessor.connectionClosing(context);
			
			try {
				router.unregister(sessionJid);
			} catch (Exception e) {
				logger.error(String.format("Can't unregister session %s from router.", sessionJid), e);
			}
			
			try {
				sessionManager.remove(sessionJid);
			} catch (Exception e) {
				logger.error(String.format("Can't remove session %s.", sessionJid), e);
			}
			
			messageProcessor.connectionClosed(context, sessionJid);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Session closed[{}].", session);
		}
	}
	
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Session idle[{}, {}].", session, status);
		}
		
		session.write(STRING_CONNECTION_TIMEOUT).await(500);
		session.write(STRING_CLOSE_STREAM).await(500);
		
		session.close(true);
		
		sessionClosed(session);
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Message received[{}, {}].", session, message);
		}
		messageProcessor.process(new SocketConnectionContext(session, localNodeIdProvider.getLocalNodeId()), new SimpleMessage(message));
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Message sent[{}, {}].", session, message);
		}
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		if (cause instanceof ProtocolDecoderException) { // the exception is thrown by message decoder
			if (logger.isDebugEnabled()) {
				logger.debug("Protocol decoder exception caught.", cause);
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
				logger.warn("Exception caught.", cause);
			}
		}
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		tlsParameter = new TlsParameter(appConfiguration.getConfigDir() + DIR_NAME_SECURITY,
				appConfiguration.getDomainName());
		messageFormat = appConfiguration.getMessageFormat();
	}

	@Override
	public IConnectionContext getConnectionContext(JabberId sessionJid) {
		ISession session = sessionManager.get(sessionJid);
		
		Object clientSessionId = null;
		if (session != null) {
			clientSessionId = session.getAttribute(StreamConstants.KEY_CLIENT_SESSION_ID);
		}
		
		if (clientSessionId == null) {
			logger.warn("Null client session ID. session JID: {}", sessionJid);		
			return null;
		}
		
		IoSession clientSession = acceptor.getManagedSessions().get(clientSessionId);
		if (clientSession != null) {
			return new SocketConnectionContext(clientSession, localNodeIdProvider.getLocalNodeId());
		}
		
		return null;
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public void init() {
		if (Constants.MESSAGE_FORMAT_BINARY.equals(messageFormat)) {
			bxmppProtocolConverter = OsgiUtils.getService(bundleContext, IBinaryXmppProtocolConverter.class);
			binaryMessagePreprocessor = OsgiUtils.getService(bundleContext, IMessagePreprocessor.class, IApplicationConfiguration.APP_CONFIG_KEY_MESSAGE_FORMAT, Constants.MESSAGE_FORMAT_BINARY);
			
			if (bxmppProtocolConverter == null || binaryMessagePreprocessor == null) {
				logger.warn("Can't get BXMPP protocol converter or binary message preprocessor. Please add gem bxmpp plugins to your plugins directory. Ignore to configure message format to binary. Still use XML message format.");
				messageFormat = Constants.NAMESPACE_XML;
			}
		}
	}
	
}
