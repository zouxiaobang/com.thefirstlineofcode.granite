package com.firstlinecode.granite.framework.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.Protocol;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stanza.error.FeatureNotImplemented;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAllowed;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stream.error.StreamError;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.oxm.parsing.FlawedProtocolObject;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.IContributionTracker;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.commons.utils.CommonUtils;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.event.EventProducer;
import com.firstlinecode.granite.framework.core.event.IEventProducer;
import com.firstlinecode.granite.framework.core.event.IEventProducerAware;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.supports.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.session.ValueWrapper;

import net.sf.cglib.proxy.UndeclaredThrowableException;

@Component("default.protocol.processing.processor")
public class DefaultProtocolProcessingProcessor implements com.firstlinecode.granite.framework.core.integration.IMessageProcessor,
		IConfigurationAware, IBundleContextAware, IInitializable, IApplicationConfigurationAware {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultProtocolProcessingProcessor.class);
	
	private static final String CONFIGURATION_KEY_STANZA_ERROR_ATTACH_SENDER_MESSAGE = "stanza.error.attach.sender.message";
	private static final String CONFIGURATION_KEY_RELAY_UNKNOWN_NAMESPACE_IQ = "relay.unknown.namespace.iq";
	
	private static final String STANZA_TYPE_PRESENCE = "presence";
	private static final String STANZA_TYPE_MESSAGE = "message";
	private static final String STANZA_TYPE_IQ = "iq";
	private static final String PROPERTY_NAME_CLASS = "class";
	private static final String PROPERTY_NAME_XEP = "xep";
	private static final String KEY_GRANITE_XEP_PROCESSORS = "Granite-Xep-Processors";
	private static final String SEPARATOR_OF_COMPONENTS = ",";
	private static final String SEPARATOR_OF_LOCALNAME_NAMESPACE = "|";
	
	protected BundleContext bundleContext;
	
	protected IPresenceProcessor presenceProcessor;
	protected com.firstlinecode.granite.framework.processing.IMessageProcessor messageProcessor;
	protected IIqResultProcessor iqResultProcessor;
	
	protected Map<ProtocolChain, BundleAndXepProcessorClass> bundleToXepProcessorClasses;
	protected Map<Bundle, List<Xep>> bundleToXeps;
	
	private boolean stanzaErrorAttachSenderMessage;
	private boolean relayUnknownNamespaceIq;
	
	private IContributionTracker xepProcessorsTracker;
	
	private IApplicationComponentService appComponentService;
	
	@Dependency("event.message.channel")
	private IMessageChannel eventMessageChannel;
	
	@Dependency("authenticator")
	private IAuthenticator authenticator;
	
	private IEventProducer eventProducer;
	
	private JabberId domain;
	private JabberId[] domainAliases;
	
	public DefaultProtocolProcessingProcessor() {
		bundleToXepProcessorClasses = new HashMap<>();
		bundleToXeps = new HashMap<>();
		
		xepProcessorsTracker = new XepProcessorsContributionTracker();
	}

	@Override
	public synchronized void init() {
		eventProducer = new EventProducer(eventMessageChannel);
		OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_XEP_PROCESSORS, xepProcessorsTracker);
	}
	
	@SuppressWarnings("unchecked")
	private class XepProcessorsContributionTracker implements IContributionTracker {

		@Override
		public void found(Bundle bundle, String contribution) throws Exception {
			StringTokenizer tokenizer = new StringTokenizer(contribution, SEPARATOR_OF_COMPONENTS);
			
			List<Xep> xeps = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				String processosrString = tokenizer.nextToken();
				
				Map<String, String> properties = CommonUtils.parsePropertiesString(processosrString,
						new String[] {PROPERTY_NAME_XEP, PROPERTY_NAME_CLASS});
				String sXep = properties.get(PROPERTY_NAME_XEP);
				if (sXep == null) {
					throw new IllegalArgumentException("Null xep[register processor].");
				}
				
				Xep xep= parseXep(bundle, sXep);
				
				String sClass = properties.get(PROPERTY_NAME_CLASS);
				if (sClass == null) {
					throw new IllegalArgumentException("Null class[register processor].");
				}
				
				Class<?> clazz = bundle.loadClass(sClass);
				
				if (!(IXepProcessor.class.isAssignableFrom(clazz))) {
					throw new IllegalArgumentException(String.format("%s must implement %s[register processor].",
							sClass, IXepProcessor.class));
				}
				
				Class<IXepProcessor<?, ?>> processorClass = (Class<IXepProcessor<?, ?>>)clazz;
				for (ProtocolChain protocolChain : xep.getProtocolChains()) {
					bundleToXepProcessorClasses.put(protocolChain, new BundleAndXepProcessorClass(bundle, processorClass));
				}
				xeps.add(xep);
			}
			
			bundleToXeps.put(bundle, xeps);
			
		}

		@Override
		public void lost(Bundle bundle, String contribution) throws Exception {
			List<Xep> xeps = bundleToXeps.remove(bundle);
			
			if (xeps == null || xeps.isEmpty())
				return;
			
			for (Xep xep : xeps) {
				for (ProtocolChain chain : xep.getProtocolChains()) {
					bundleToXepProcessorClasses.remove(chain);
				}
			}
		}
		
	}
	
	private class BundleAndXepProcessorClass {
		public Bundle bundle;
		public Class<IXepProcessor<?, ?>> xepProcessorClass;
		
		public BundleAndXepProcessorClass(Bundle bundle, Class<IXepProcessor<?, ?>> xepProcessorClass) {
			this.bundle = bundle;
			this.xepProcessorClass = xepProcessorClass;
		}
	}

	private Xep parseXep(Bundle bundle, String sXep) {
		int separatorIndex = sXep.indexOf("->");
		if (separatorIndex == -1 || separatorIndex == sXep.length() - 2) {
			throw new IllegalArgumentException(String.format("Invalid xep name %s[register processor].", sXep));
		}
		
		String sStanzaType = sXep.substring(0, separatorIndex);
		Xep.StanzaType stanzaType = getStanzaType(sStanzaType);
		
		if (stanzaType == null)
			throw new IllegalArgumentException(String.format("Unknown stanza type %s[register processor].", sStanzaType));
		
		String sProtocols = sXep.substring(separatorIndex + 2);
		List<Protocol> protocols = parseProtocols(sProtocols);
		
		return new Xep(stanzaType, protocols);
	}

	private Xep.StanzaType getStanzaType(String sStanzaType) {
		Xep.StanzaType stanzaType = null;
		
		if (STANZA_TYPE_IQ.equals(sStanzaType)) {
			stanzaType = Xep.StanzaType.IQ;
		} else if (STANZA_TYPE_MESSAGE.equals(sStanzaType)) {
			stanzaType = Xep.StanzaType.MESSAGE;
		} else if (STANZA_TYPE_PRESENCE.equals(sStanzaType)) {
			stanzaType = Xep.StanzaType.PRESENCE;
		}
		
		return stanzaType;
	}

	private List<Protocol> parseProtocols(String sProtocols) {
		StringTokenizer tokenizer = new StringTokenizer(sProtocols, "&");
		
		List<Protocol> protocols = new ArrayList<>();
		while (tokenizer.hasMoreTokens()) {
			String sProtocol = tokenizer.nextToken();
	 		String localName;
			String namespace;
			
			int seperator = sProtocol.indexOf(SEPARATOR_OF_LOCALNAME_NAMESPACE);
			if (seperator == -1) {
				throw new IllegalArgumentException(String.format("Invalid protocol[%s].", sProtocol));
			}
			
			localName = sProtocol.substring(0, seperator).trim();
			namespace = sProtocol.substring(seperator + 1, sProtocol.length()).trim();
			
			if (localName.length() == 0 || namespace.length() == 0) {
				throw new IllegalArgumentException(String.format("Invalid protocol[%s].", sProtocol));
			}
			
			protocols.add(new Protocol(namespace, localName));
		}
		
		return protocols;
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
	}
	
	@Override
	public void process(IConnectionContext context, IMessage message) {
		try {
			doProcess((IProcessingContext)context, message);
		} catch (ProtocolException e) {
			processProtocolException(context, e, message.getPayload());
		} catch (RuntimeException e) {
			processRuntimeException(context, e, message.getPayload());
		} catch (ComplexStanzaProtocolException e) {
			for (Exception exception : e.getExceptions()) {
				if (exception instanceof ProtocolException) {
					processProtocolException(context, (ProtocolException)exception, message.getPayload());
				} else {
					// exception instanceof RuntimeException
					processRuntimeException(context, (RuntimeException)exception, message.getPayload());
				}
			}
		}
	}

	private void processRuntimeException(IConnectionContext context, RuntimeException e, Object message) {
		if (e instanceof UndeclaredThrowableException) {
			ProtocolException pe = findProtocolException(e);
			
			if(pe != null) {
				processProtocolException(context, pe, message);
				return;
			}
		}
		
		outputRuntimeExceptionError(context, e, message);
	}

	private ProtocolException findProtocolException(Throwable e) {
		Throwable current = e;
		
		while (current.getCause() != null) {
			if (current.getCause() instanceof ProtocolException) {
				return (ProtocolException)current.getCause();
			}
			
			current = current.getCause();
		}
		
		return null;
	}

	private void outputRuntimeExceptionError(IConnectionContext context, RuntimeException e, Object message) {
		if (message instanceof Stanza) {
			context.write(createStanzaError(context, e, message));
		} else {
			context.write(new com.firstlinecode.basalt.protocol.core.stream.error.InternalServerError(CommonUtils.getInternalServerErrorMessage(e)));
			context.close();
		}
		
		logger.error("Processing error.", e);
	}

	private StanzaError createStanzaError(IConnectionContext context, RuntimeException e, Object message) {
		Stanza stanza = (Stanza)message;
		StanzaError error = new com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError(CommonUtils.getInternalServerErrorMessage(e));
		
		error = amendStanzaError(context, error, stanza);
		
		return error;
	}

	private StanzaError amendStanzaError(IConnectionContext context, StanzaError error, Stanza stanza) {
		if (error.getKind() == null) {
			if (stanza instanceof Message) {
				error.setKind(StanzaError.Kind.MESSAGE);
			} else if (stanza instanceof Presence) {
				error.setKind(StanzaError.Kind.PRESENCE);
			} else {
				error.setKind(StanzaError.Kind.IQ);
			}
		}
		
		if (error.getId() == null) {
			error.setId(stanza.getId());
		}
		
		if (error.getTo() == null) {
			JabberId to = stanza.getFrom() == null ? context.getJid() : stanza.getFrom();
			error.setTo(to);
		}
		
		if (error.getFrom() == null) {
			error.setFrom(stanza.getTo() == null ? domain : stanza.getTo());
		}
		
		return error;
	}

	private void processProtocolException(IConnectionContext context, ProtocolException e, Object message) {
		IError error = e.getError();
		
		if ((StanzaError.class.isAssignableFrom(error.getClass())) && (message instanceof Stanza)) {
			error = amendStanzaError(context, (StanzaError)error, (Stanza)message);
		}
		
		context.write(e.getError());
		
		if (error instanceof StreamError) {
			context.close();
		}
	}
	
	private class AttachOriginalMessageConnectionContextProxy implements IProcessingContext {
		private IProcessingContext original;
		private String originalMessage;
		
		public AttachOriginalMessageConnectionContextProxy(IProcessingContext original,
				String originalMessage) {
			this.original = original;
			this.originalMessage = originalMessage;
		}

		@Override
		public <T> T setAttribute(Object key, T value) {
			return original.setAttribute(key, value);
		}

		@Override
		public <T> T getAttribute(Object key) {
			return original.getAttribute(key);
		}

		@Override
		public <T> T getAttribute(Object key, T defaultValue) {
			return original.getAttribute(key, defaultValue);
		}

		@Override
		public <T> T removeAttribute(Object key) {
			return original.removeAttribute(key);
		}

		@Override
		public Object[] getAttributeKeys() {
			return original.getAttributeKeys();
		}

		@Override
		public JabberId getJid() {
			return original.getJid();
		}

		@Override
		public void write(Object message) {
			if (message instanceof StanzaError) {
				((StanzaError)message).setOriginalMessage(originalMessage);
			}
			
			original.write(message);
		}

		@Override
		public void close() {
			original.close();
		}

		@Override
		public void write(JabberId target, Object message) {
			if (message instanceof StanzaError) {
				((StanzaError)message).setOriginalMessage(originalMessage);
			}
			
			original.write(target, message);
		}

		@Override
		public <T> T setAttribute(Object key, ValueWrapper<T> wrapper) {
			return original.setAttribute(key, wrapper);
		}
		
	}

	private void doProcess(IProcessingContext context, IMessage message) throws RuntimeException,
				ComplexStanzaProtocolException {
		Object object = message.getPayload();
		
		if (stanzaErrorAttachSenderMessage && (object instanceof Stanza)) {
			String originalMessage = ((Stanza)object).getOriginalMessage();
			context = new AttachOriginalMessageConnectionContextProxy(context, originalMessage);
		}
		
		if ((object instanceof Stanza) && logger.isTraceEnabled()) {
			logger.trace("Processing stanza message. Original message: {}.", ((Stanza)object).getOriginalMessage());
		}
		
		if (object instanceof Stanza) {
			Stanza stanza = (Stanza)object;
			
			// try to process stanza in a more simply way if it's a simple structure stanza. 
			if (processSimpleStanza(context, stanza)) {
				return;
			}
			
			processComplexStanza(context, stanza);
		} else {
			context.write(object);
		}
	}

	private void processComplexStanza(IProcessingContext context, Stanza stanza)
			throws ComplexStanzaProtocolException {
		boolean processed = false;
		List<Exception> exceptions = new ArrayList<>();
		for (Object object : stanza.getObjects()) {
			if (object instanceof FlawedProtocolObject) {
				continue;
			}
			
			try {
				processXep(context, stanza, object);
				processed = true;
			} catch (Exception e) {
				if (e instanceof ProtocolException) {
					ProtocolException pe = ((ProtocolException)e);
					// ServiceUnavailable error will be thrown if protocol is not a supported XEP.
					if (!(pe.getError() instanceof ServiceUnavailable)) {
						processed = true;
					}
				}
				exceptions.add(e);
			}
		}
		
		if (FlawedProtocolObject.isFlawed(stanza)) {
			FlawedProtocolObject flawed = stanza.getObject(FlawedProtocolObject.class);
			if (flawed != null) {
				for (ProtocolChain protocolChain : flawed.getFlaws()) {
					// we find top level protocol object that are embedded into stanza instantly.
					if (protocolChain.size() == 2) {
						exceptions.add(new ProtocolException(new ServiceUnavailable(
								String.format("Unsupported protocol: %s.", protocolChain))));
					}
				}
			}
		}
		
		if (!processed) {
			processed = processStanza(context, stanza);
		}
		
		if (!processed) {
			throw new ComplexStanzaProtocolException(exceptions);
		}
	}
	
	private boolean processStanza(IProcessingContext context, Stanza stanza) {
		if (stanza instanceof Presence) {
			return processPresence(context, (Presence)stanza);
		} else if (stanza instanceof Message) {
			return processMessage(context, (Message)stanza);
		} else {
			return processIq(context, (Iq)stanza);
		}
	}

	private boolean processIq(IProcessingContext context, Iq iq) {
		if (!relayUnknownNamespaceIq)
			return false;
		
		if (!isServerReceipt(iq))
			return false;
		
		String userName = iq.getTo().getNode();
		if (userName == null)
			return false;
		
		if (!authenticator.exists(userName)) {
			return false;
		}
		
		context.write(iq);
		return true;
	}

	private class ComplexStanzaProtocolException extends Exception {
		private static final long serialVersionUID = 946967617544711594L;
		
		private List<Exception> exceptions;
		
		public ComplexStanzaProtocolException(List<Exception> exceptions) {
			this.exceptions = exceptions;
		}
		
		public List<Exception> getExceptions() {
			return exceptions;
		}
	}

	private boolean processSimpleStanza(IProcessingContext context, Stanza stanza) {
		if (stanza instanceof Presence) {
			if (!stanza.getObjects().isEmpty())
				return false;
			
			if (!processPresence(context, (Presence)stanza)) {
				throw new ProtocolException(new ServiceUnavailable());
			}
			
			return true;
		} else if (stanza instanceof Message) {
			if (!stanza.getObjects().isEmpty())
				return false;
				
			if (!processMessage(context, (Message)stanza)) {
				throw new ProtocolException(new ServiceUnavailable());
			}
			
			return true;
		} else if (stanza instanceof Iq) {
			if (stanza.getObjects().isEmpty()) {
				processIqResult(context, (Iq)stanza);
				return true;
			}
			
			if (stanza.getObjects().size() != 1)
				return false;
			
			if (FlawedProtocolObject.isFlawed(stanza)) {
				return false;
			}
			
			processXep(context, (Iq)stanza, stanza.getObject());
			return true;
		} else {
			// stanza instanceof StanzaError
			context.write(stanza);
			return true;
		}
	}
	
	private void processIqResult(IProcessingContext context, Iq iq) {
		if (iq.getType() != Iq.Type.RESULT)
			throw new ProtocolException(new BadRequest("Neither XEP nor IQ result."));
		
		if (iq.getId() == null) {
			throw new ProtocolException(new BadRequest("Null ID."));
		}
		
		if (iqResultProcessor == null) {
			throw new ProtocolException(new ServiceUnavailable());
		}
		
		iqResultProcessor.process(context, iq);
	}

	private boolean processMessage(IProcessingContext context, Message message) {
		if (messageProcessor != null && messageProcessor.process(context, message)) {
			return true;
		}
		
		return false;
	}

	private boolean processPresence(IProcessingContext context, Presence presence) {
		if (presenceProcessor != null) {
			return presenceProcessor.process(context, presence);
		}
		
		return false;
	}
	
	private <K extends Stanza, V> void processXep(IProcessingContext context, Stanza stanza, Object xep) throws RuntimeException {
		if (isServerReceipt(stanza)) {
			doProcessXep(context, stanza, xep);
			return;
		}
		
		if (isToForeignDomain(stanza.getTo())) {
			deliverXepToForeignDomain(context, stanza);
		}
	}

	private boolean isServerReceipt(Stanza stanza) {
		return stanza.getTo() == null || isToDomain(stanza.getTo()) || isToDomainAlias(stanza.getTo());
	}

	private boolean isToDomain(JabberId to) {
		return to.getDomain().equals(domain.getDomain());
	}
	
	private boolean isToDomainAlias(JabberId to) {
		if (domainAliases.length == 0)
			return false;
		
		for (JabberId domainAlias : domainAliases) {
			if (domainAlias.getDomain().equals(to.getDomain()))
				return true;
		}
		
		return false;
	}

	private void deliverXepToForeignDomain(IProcessingContext context, Stanza stanza) {
		if (stanza.getFrom() != null && !stanza.getFrom().equals(context.getJid())) {
			throw new ProtocolException(new NotAllowed(String.format("'from' attribute should be %s.", context.getJid())));
		}
		
		// TODO Server Rules for Handling XML Stanzas(rfc3920 10)
		throw new ProtocolException(new FeatureNotImplemented("Feature delivering XEP to foreign domain isn't implemented yet."));
	}

	private boolean isToForeignDomain(JabberId to) {
		if (to == null)
			return false;
		
		JabberId toDomain = new JabberId(to.getDomain());
		if (isToDomain(toDomain) || isToDomainAlias(toDomain))
			return false;
		
		return true;
	}

	@SuppressWarnings("unchecked")
	private <V, K extends Stanza> boolean doProcessXep(IProcessingContext context, Stanza stanza, Object xep) {
		ProtocolChain protocolChain = getXepProtocolChain(stanza, xep);
		BundleAndXepProcessorClass baxpc = bundleToXepProcessorClasses.get(protocolChain);		
		if (baxpc == null) {
			throw new ProtocolException(new ServiceUnavailable(String.format("Unsupported protocol: %s.",
					stanza.getObjectProtocol(stanza.getObject().getClass()))));
		}
		
		IXepProcessor<K, V> xepProcessor;
		try {
			xepProcessor = (IXepProcessor<K, V>)baxpc.xepProcessorClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Can't instantiate XEP processor.", e);
		}
		
		appComponentService.inject(xepProcessor, baxpc.bundle.getBundleContext());
		
		if (xepProcessor instanceof IEventProducerAware) {
			((IEventProducerAware)xepProcessor).setEventProducer(eventProducer);
		}
		
		xepProcessor.process(context, (K)stanza, (V)xep);
		
		return true;
	}

	private ProtocolChain getXepProtocolChain(Stanza stanza, Object xep) {
		return ProtocolChain.first(getStanzaProtocol(stanza)).next(stanza.getObjectProtocol(xep.getClass()));
	}

	private Protocol getStanzaProtocol(Stanza stanza) {
		if (stanza instanceof Iq)
			return Iq.PROTOCOL;
		
		if (stanza instanceof Presence)
			return Presence.PROTOCOL;
		
		if (stanza instanceof Message)
			return Message.PROTOCOL;
		
		return null;
	}

	@Dependency("presence.processor")
	public void setPresenceProcessor(IPresenceProcessor presenceProcessor) {
		this.presenceProcessor = presenceProcessor;
	}
	
	@Dependency("message.processor")
	public void setMessageProcessor(com.firstlinecode.granite.framework.processing.IMessageProcessor messageProcessor) {
		this.messageProcessor = messageProcessor;
	}
	
	@Dependency("iq.result.processor")
	public void setIqProcessor(IIqResultProcessor iqResultProcessor) {
		this.iqResultProcessor = iqResultProcessor;
	}
	
	private static class Xep {
		public enum StanzaType {
			IQ,
			PRESENCE,
			MESSAGE
		}
		
		private StanzaType stanzaType;
		private List<Protocol> protocols;
		
		public Xep(StanzaType stanzaType, List<Protocol> protocols) {
			this.stanzaType = stanzaType;
			this.protocols = protocols;
		}
		
		public List<ProtocolChain> getProtocolChains() {
			Protocol stanzaProtocol = null;
			
			if (stanzaType == StanzaType.IQ) {
				stanzaProtocol = Iq.PROTOCOL;
			} else if (stanzaType == StanzaType.MESSAGE) {
				stanzaProtocol = Message.PROTOCOL;
			} else {
				stanzaProtocol = Presence.PROTOCOL;
			}
			
			List<ProtocolChain> protocolChains = new ArrayList<>();
			for (Protocol protocol : protocols) {
				protocolChains.add(ProtocolChain.first(stanzaProtocol).next(protocol));
			}
			
			return protocolChains;
		}
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		stanzaErrorAttachSenderMessage = configuration.getBoolean(
			CONFIGURATION_KEY_STANZA_ERROR_ATTACH_SENDER_MESSAGE,
				false);
		relayUnknownNamespaceIq = configuration.getBoolean(
				CONFIGURATION_KEY_RELAY_UNKNOWN_NAMESPACE_IQ,
				false);
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		domain = JabberId.parse(appConfiguration.getDomainName());
		String[] sDomainAliasNames = appConfiguration.getDomainAliasNames();
		if (sDomainAliasNames.length != 0) {
			domainAliases = new JabberId[sDomainAliasNames.length];
			
			for (int i = 0; i < sDomainAliasNames.length; i++) {
				domainAliases[i] = JabberId.parse(sDomainAliasNames[i]);
			}
		} else {
			domainAliases = new JabberId[0];
		}
	}
}
