package com.firstlinecode.granite.stream.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.annotation.AnnotatedParserFactory;
import com.firstlinecode.basalt.oxm.parsers.core.stream.StreamParser;
import com.firstlinecode.basalt.oxm.parsing.IParsingFactory;
import com.firstlinecode.basalt.oxm.translating.ITranslatingFactory;
import com.firstlinecode.basalt.oxm.translators.core.stream.StreamTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.error.StanzaErrorTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.error.StreamErrorTranslatorFactory;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.LangText;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stream.Bind;
import com.firstlinecode.basalt.protocol.core.stream.Feature;
import com.firstlinecode.basalt.protocol.core.stream.Session;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.StreamError;
import com.firstlinecode.basalt.protocol.core.stream.sasl.Mechanisms;
import com.firstlinecode.basalt.protocol.core.stream.tls.StartTls;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.commons.utils.CommonUtils;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManager;
import com.firstlinecode.granite.framework.core.connection.IConnectionManagerAware;
import com.firstlinecode.granite.framework.core.event.ConnectionClosedEvent;
import com.firstlinecode.granite.framework.core.event.ConnectionOpenedEvent;
import com.firstlinecode.granite.framework.core.pipes.IClientMessageProcessor;
import com.firstlinecode.granite.framework.core.pipes.IMessage;
import com.firstlinecode.granite.framework.core.pipes.IMessageChannel;
import com.firstlinecode.granite.framework.core.pipes.SimpleMessage;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.routing.IRouter;
import com.firstlinecode.granite.framework.core.session.ISessionListener;
import com.firstlinecode.granite.framework.core.session.ISessionManager;
import com.firstlinecode.granite.pipes.stream.IStreamNegotiant;
import com.firstlinecode.granite.pipes.stream.StreamConstants;
import com.firstlinecode.granite.pipes.stream.negotiants.InitialStreamNegotiant;
import com.firstlinecode.granite.pipes.stream.negotiants.ResourceBindingNegotiant;
import com.firstlinecode.granite.pipes.stream.negotiants.SaslNegotiant;
import com.firstlinecode.granite.pipes.stream.negotiants.SessionEstablishmentNegotiant;
import com.firstlinecode.granite.pipes.stream.negotiants.TlsNegotiant;

@Component("standard.client.message.processor")
public class StandardClientMessageProcessor implements IClientMessageProcessor, IConfigurationAware,
		IServerConfigurationAware, IApplicationComponentServiceAware, IInitializable {
	private static final Logger logger = LoggerFactory.getLogger(StandardClientMessageProcessor.class);
	
	private static final String CONFIGURATION_KEY_TLS_REQUIRED = "tls.required";
	private static final String CONFIGURATION_KEY_SASL_FAILURE_RETRIES = "sasl.failure.retries";
	private static final String CONFIGURATION_KEY_SASL_ABORT_RETRIES = "sasl.abort.retries";
	private static final String CONFIGURATION_KEY_SASL_SUPPORTED_MECHANISMS = "sasl.supported.mechanisms";
	
	private static final Object KEY_NEGOTIANT = "granite.key.negotiant";
	
	private IConnectionManager connectionManager;

	private IParsingFactory parsingFactory;
	private ITranslatingFactory translatingFactory;
	
	protected IAuthenticator authenticator;	
	protected ISessionManager sessionManager;
	protected IMessageChannel messageChannel;
	protected IMessageChannel eventMessageChannel;
	protected IRouter router;
	
	protected String hostName;
	
	protected boolean tlsRequired;
	
	protected int saslAbortRetries;
	protected int saslFailureRetries;
	protected String[] saslSupportedMechanisms;
	
	protected ISessionListener sessionListenerDelegate;
	
	private List<ISessionListener> sessionListeners;
	
	private IApplicationComponentService appComponentService;
	
	public StandardClientMessageProcessor() {
		parsingFactory = OxmService.createParsingFactory();
		parsingFactory.register(ProtocolChain.first(Stream.PROTOCOL), new AnnotatedParserFactory<>(StreamParser.class));
		
		translatingFactory = OxmService.createTranslatingFactory();
		translatingFactory.register(Stream.class, new StreamTranslatorFactory());
		translatingFactory.register(StreamError.class, new StreamErrorTranslatorFactory());
		translatingFactory.register(StanzaError.class, new StanzaErrorTranslatorFactory());
		
		sessionListeners = new ArrayList<>();
		sessionListenerDelegate = new SessionListenerDelegate();
	}
	
	private class SessionListenerDelegate implements ISessionListener {

		@Override
		public void sessionEstablishing(IConnectionContext context, JabberId sessionJid) throws Exception {
			for (ISessionListener sessionListener : sessionListeners) {
				sessionListener.sessionEstablishing(context, sessionJid);
			}
		}
		
		@Override
		public void sessionEstablished(IConnectionContext context, JabberId sessionJid) throws Exception {
			for (ISessionListener sessionListener : sessionListeners) {
				sessionListener.sessionEstablished(context, sessionJid);
			}
		}

		@Override
		public void sessionClosing(IConnectionContext context, JabberId sessionJid) throws Exception {
			for (ISessionListener sessionListener : sessionListeners) {
				sessionListener.sessionClosing(context, sessionJid);
			}
		}

		@Override
		public void sessionClosed(IConnectionContext context, JabberId sessionJid) throws Exception {
			for (ISessionListener sessionListener : sessionListeners) {
				sessionListener.sessionClosed(context, sessionJid);
			}
		}
		
	}

	@Override
	public void process(IConnectionContext context, IMessage message) {
		doProcess((IClientConnectionContext)context, message);
		
	}
	
	private void doProcess(IClientConnectionContext context, IMessage message) {
		if (isCloseStreamRequest((String)message.getPayload())) {
			context.write(translatingFactory.translate(new Stream(true)));
			context.close();
			return;
		}
				
		JabberId jid = context.getAttribute(StreamConstants.KEY_SESSION_JID);
		if (jid != null) {
			Map<Object, Object> headers = new HashMap<>();
			headers.put(IMessage.KEY_SESSION_JID, jid);
			IMessage out = new SimpleMessage(headers, message.getPayload());
			
			messageChannel.send(out);
		} else {
			IStreamNegotiant negotiant = context.getAttribute(KEY_NEGOTIANT);
			if (negotiant == null) {
				negotiant = createNegotiant();
				context.setAttribute(KEY_NEGOTIANT, negotiant);
			}
			
			try {
				if (negotiant.negotiate(context, message)) {
					context.removeAttribute(KEY_NEGOTIANT);
				}
			} catch (ProtocolException e) {
				context.write(translatingFactory.translate(e.getError()));
				if (e.getError() instanceof StreamError) {
					closeStream(context);
				}
			} catch (RuntimeException e) {
				logger.warn("Negotiation error.", e);
				
				InternalServerError error = new InternalServerError();
				error.setText(new LangText(String.format("Negotiation error. %s.",
						CommonUtils.getInternalServerErrorMessage(e))));
				context.write(translatingFactory.translate(error));
				closeStream(context);
			}
			
		}
	}

	private void fireConnectionOpenedEvent(IClientConnectionContext context) {
		ConnectionOpenedEvent event = new ConnectionOpenedEvent(context.getConnectionId().toString(),
				context.getRemoteIp(), context.getRemotePort());	
		eventMessageChannel.send(new SimpleMessage(event));
	}
	
	private void fireConnectionClosedEvent(IClientConnectionContext context) {
		ConnectionClosedEvent event = new ConnectionClosedEvent(context.getConnectionId().toString(),
				context.getJid(), context.getStreamId());	
		eventMessageChannel.send(new SimpleMessage(event));
	}

	private boolean isCloseStreamRequest(String message) {
		try {
			Object object = parsingFactory.parse(message, true);
			if (object instanceof Stream) {
				return ((Stream)object).isClose();
			}
			
			return false;			
		} catch (Exception e) {
			return false;
		}

	}

	private void closeStream(IConnectionContext context) {
		context.write(translatingFactory.translate(new Stream(true)));
		context.close();
	}

	protected IStreamNegotiant createNegotiant() {
		IStreamNegotiant intialStream = new InitialStreamNegotiant(hostName,
				getInitialStreamNegotiantAdvertisements());
		
		IStreamNegotiant tls = new TlsNegotiant(hostName, tlsRequired,
				getTlsNegotiantAdvertisements());
		
		IStreamNegotiant sasl = new SaslNegotiant(hostName,
				saslSupportedMechanisms, saslAbortRetries, saslFailureRetries,
				getSaslNegotiantFeatures(), authenticator);
		
		IStreamNegotiant resourceBinding = new ResourceBindingNegotiant(
				hostName, sessionManager);
		IStreamNegotiant sessionEstablishment = new SessionEstablishmentNegotiant(
				router, sessionManager, eventMessageChannel, sessionListenerDelegate);
		
		resourceBinding.setNext(sessionEstablishment);
		sasl.setNext(resourceBinding);
		tls.setNext(sasl);
		intialStream.setNext(tls);
		
		return intialStream;
	}

	private String[] parseSupportedMechanisms(String sMechanisms) {
		StringTokenizer st = new StringTokenizer(sMechanisms, ",");
		
		if (st.countTokens() == 0) {
			throw new IllegalArgumentException(String.format("Can't determine supported sasl mechanisms: %s.", sMechanisms));
		}
		
		String[] mechanisms = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			mechanisms[i] = st.nextToken().trim();
			i++;
		}
		
		return mechanisms;
	}

	protected List<Feature> getSaslNegotiantFeatures() {
		List<Feature> features = new ArrayList<>();
		features.add(new Bind());
		features.add(new Session());
		
		return features;
	}

	protected List<Feature> getTlsNegotiantAdvertisements() {
		List<Feature> features = new ArrayList<>();
		
		Mechanisms mechanisms = new Mechanisms();
		for (String supportedMechanism : saslSupportedMechanisms) {
			mechanisms.getMechanisms().add(supportedMechanism);
		}
		
		features.add(mechanisms);
		
		return features;
	}

	protected List<Feature> getInitialStreamNegotiantAdvertisements() {
		List<Feature> features = new ArrayList<>();
		
		StartTls startTls = new StartTls();
		if (tlsRequired) {
			startTls.setRequired(true);
		}
		features.add(startTls);
		
		features.addAll(getTlsNegotiantAdvertisements());
		
		return features;
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		hostName = serverConfiguration.getDomainName();
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		tlsRequired = configuration.getBoolean(CONFIGURATION_KEY_TLS_REQUIRED, true);
		
		saslSupportedMechanisms = parseSupportedMechanisms(configuration.getString(
				CONFIGURATION_KEY_SASL_SUPPORTED_MECHANISMS, "PLAIN,DIGEST-MD5"));
		saslAbortRetries = configuration.getInteger(CONFIGURATION_KEY_SASL_ABORT_RETRIES, 3);
		saslFailureRetries = configuration.getInteger(CONFIGURATION_KEY_SASL_FAILURE_RETRIES, 3);
	}

	@Override
	public void connectionOpened(IClientConnectionContext context) {
		fireConnectionOpenedEvent(context);
	}
	
	@Override
	public void connectionClosing(IClientConnectionContext context) {
		try {
			sessionListenerDelegate.sessionClosing(context, context.getJid());
		} catch (Exception e) {
			logger.error("Some errors occurred in session closing callback method.", e);
		}
	}

	@Override
	public void connectionClosed(IClientConnectionContext context, JabberId sessionJid) {
		try {
			sessionListenerDelegate.sessionClosed(context, sessionJid);
		} catch (Exception e) {
			logger.error("Some errors occurred in session closed callback method.", e);
		}
		
		fireConnectionClosedEvent(context);
	}
	
	@Dependency("authenticator")
	public void setAuthenticator(IAuthenticator authenticator) {
		this.authenticator = authenticator;
	}
	
	@Dependency("session.manager")
	public void setSessionManager(ISessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	@Dependency("message.channel")
	public void setMessageChannel(IMessageChannel messageChannel) {
		this.messageChannel = messageChannel;
	}
	
	@Dependency("event.message.channel")
	public void setEventMessageChannel(IMessageChannel eventMessageChannel) {
		this.eventMessageChannel = eventMessageChannel;
	}
	
	@Dependency("router")
	public void setRouter(IRouter router) {
		this.router = router;
	}

	@Override
	public void init() {
		loadContributedSessionListeners();
	}
	
	private void loadContributedSessionListeners() {
		List<Class<? extends ISessionListener>> sessionListenerClasses = appComponentService.
				getExtensionClasses(ISessionListener.class);
		if (sessionListenerClasses == null || sessionListenerClasses.size() == 0) {
			if (logger.isDebugEnabled())
				logger.debug("No extension which's extension point is {} found.", ISessionListener.class.getName());
			
			return;
		}
		
		for (Class<? extends ISessionListener> sessionListenerClass : sessionListenerClasses) {
			ISessionListener sessionListener = appComponentService.createRawExtension(sessionListenerClass);
			if (sessionListener instanceof IConnectionManagerAware) {
				((IConnectionManagerAware)sessionListener).setConnectionManager(connectionManager);
			}
			
			appComponentService.inject(sessionListener);
			sessionListeners.add(sessionListener);
		}
	}

	@Override
	public void setConnectionManager(IConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

}
