package com.firstlinecode.granite.pipes.processing;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.oxm.parsing.FlawedProtocolObject;
import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.Protocol;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stanza.error.FeatureNotImplemented;
import com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAllowed;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stream.error.StreamError;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.annotations.BeanDependency;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.pipes.IMessage;
import com.firstlinecode.granite.framework.core.pipes.IPipesExtendersContributor;
import com.firstlinecode.granite.framework.core.pipes.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.core.pipes.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.pipes.processing.IXepProcessor;
import com.firstlinecode.granite.framework.core.pipes.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.session.ValueWrapper;
import com.firstlinecode.granite.framework.core.utils.CommonsUtils;
import com.firstlinecode.granite.framework.im.IPresenceProcessor;

@Component("default.protocol.processing.processor")
public class DefaultProtocolProcessingProcessor implements com.firstlinecode.granite.framework.core.pipes.IMessageProcessor,
		IConfigurationAware, IInitializable, IServerConfigurationAware, IApplicationComponentServiceAware {
	private static final Logger logger = LoggerFactory.getLogger(DefaultProtocolProcessingProcessor.class);
	
	private static final String CONFIGURATION_KEY_STANZA_ERROR_ATTACH_SENDER_MESSAGE = "stanza.error.attach.sender.message";
	private static final String CONFIGURATION_KEY_RELAY_UNKNOWN_NAMESPACE_IQ = "relay.unknown.namespace.iq";
	
	protected IPresenceProcessor presenceProcessor;
	protected com.firstlinecode.granite.framework.im.IMessageProcessor messageProcessor;
	protected IIqResultProcessor iqResultProcessor;
	
	protected Map<ProtocolChain, IXepProcessor<?, ?>> singletonProcessores;
	protected Map<ProtocolChain, IXepProcessorFactory<?, ?>> xepProcessorFactories;
	
	private boolean stanzaErrorAttachSenderMessage;
	private boolean relayUnknownNamespaceIq;
	
	private IApplicationComponentService appComponentService;
	
	@BeanDependency
	private IAuthenticator authenticator;
	
	private JabberId domain;
	private JabberId[] domainAliases;
	
	public DefaultProtocolProcessingProcessor() {
		xepProcessorFactories = new HashMap<>();
		singletonProcessores = new HashMap<>();
	}

	@Override
	public synchronized void init() {
		loadContributedXepProcessors();
	}
	
	protected void loadContributedXepProcessors() {
		IPipesExtendersContributor[] extendersContributors = CommonsUtils.getExtendersContributors(appComponentService);
		
		for (IPipesExtendersContributor extendersContributor : extendersContributors) {
			IXepProcessorFactory<?, ?>[] processorFactories = extendersContributor.getXepProcessorFactories();
			if (processorFactories == null || processorFactories.length == 0)
				continue;
			
			for (IXepProcessorFactory<?, ?> processorFactory : processorFactories) {
				if (processorFactory.isSingleton()) {
					IXepProcessor<?, ?> xepProcessor;
					try {
						xepProcessor = processorFactory.createProcessor();
					} catch (Exception e) {
						logger.error("Can't create singleton XEP processor by factory: '{}'.",
								processorFactory.getClass().getName(), e);
						
						throw new RuntimeException(String.format("Can't create singleton XEP processor by factory: '%s'",
								processorFactory.getClass().getName()), e);
					}
					
					singletonProcessores.put(processorFactory.getProtocolChain(), xepProcessor);
				} else {				
					xepProcessorFactories.put(processorFactory.getProtocolChain(), processorFactory);
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("Plugin '{}' contributed a protocol processor factory: '{}'.",
							appComponentService.getPluginManager().whichPlugin(extendersContributor.getClass()),
							processorFactory.getClass().getName()
					);
				}
			}
		}
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
			context.write(new com.firstlinecode.basalt.protocol.core.stream.error.InternalServerError(CommonsUtils.getInternalServerErrorMessage(e)));
			context.close();
		}
		
		logger.error("Processing error.", e);
	}

	private StanzaError createStanzaError(IConnectionContext context, RuntimeException e, Object message) {
		Stanza stanza = (Stanza)message;
		StanzaError error = new com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError(CommonsUtils.getInternalServerErrorMessage(e));
		
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
		
		iqResultProcessor.processResult(context, iq);
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
	private <K extends Stanza, V> boolean doProcessXep(IProcessingContext context, Stanza stanza, Object xep) {
		ProtocolChain protocolChain = getXepProtocolChain(stanza, xep);
		
		IXepProcessor<K, V> xepProcessor = getXepProcessor(protocolChain);
		if (xepProcessor == null) {
			throw new ProtocolException(new ServiceUnavailable(String.format("Unsupported protocol: %s.",
					stanza.getObjectProtocol(stanza.getObject().getClass()))));
		}

		xepProcessor.process(context, (K)stanza, (V)xep);
		
		return true;
	}

	@SuppressWarnings("unchecked")
	private <K extends Stanza, V> IXepProcessor<K, V> getXepProcessor(ProtocolChain protocolChain) {
		IXepProcessor<K, V> xepProcessor = (IXepProcessor<K, V>)singletonProcessores.get(protocolChain);
		
		if (xepProcessor == null) {
			xepProcessor = createXepProcessorByFactory(protocolChain);
		}
		
		return xepProcessor;
	}

	@SuppressWarnings("unchecked")
	private <V, K extends Stanza> IXepProcessor<K, V> createXepProcessorByFactory(ProtocolChain protocolChain) {
		IXepProcessorFactory<?, ?> processorFactory = xepProcessorFactories.get(protocolChain);		
		try {
			IXepProcessor<K, V> xepProcessor = (IXepProcessor<K, V>)processorFactory.createProcessor();
			appComponentService.inject(xepProcessor);
			
			return xepProcessor;
		} catch (Exception e) {
			logger.error("Can't instantiate XEP processor by factory: '{}'.",
					processorFactory.getClass().getName(), e);
			throw new ProtocolException(new InternalServerError(
					String.format("Can't instantiate XEP processor by factory: '%s'.",
					processorFactory.getClass().getName()), e));
		}
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
	public void setMessageProcessor(com.firstlinecode.granite.framework.im.IMessageProcessor messageProcessor) {
		this.messageProcessor = messageProcessor;
	}
	
	@Dependency("iq.result.processor")
	public void setIqProcessor(IIqResultProcessor iqResultProcessor) {
		this.iqResultProcessor = iqResultProcessor;
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
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domain = JabberId.parse(serverConfiguration.getDomainName());
		String[] sDomainAliasNames = serverConfiguration.getDomainAliasNames();
		if (sDomainAliasNames.length != 0) {
			domainAliases = new JabberId[sDomainAliasNames.length];
			
			for (int i = 0; i < sDomainAliasNames.length; i++) {
				domainAliases[i] = JabberId.parse(sDomainAliasNames[i]);
			}
		} else {
			domainAliases = new JabberId[0];
		}
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}
}
