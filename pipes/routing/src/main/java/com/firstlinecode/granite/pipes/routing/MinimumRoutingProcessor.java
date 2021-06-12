package com.firstlinecode.granite.pipes.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.parsing.FlawedProtocolObject;
import com.firstlinecode.basalt.oxm.translating.ITranslatingFactory;
import com.firstlinecode.basalt.oxm.translating.ITranslator;
import com.firstlinecode.basalt.oxm.translating.ITranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.core.stanza.IqTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.core.stream.StreamTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.error.StanzaErrorTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.error.StreamErrorTranslatorFactory;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.StreamError;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.pipes.IMessage;
import com.firstlinecode.granite.framework.core.pipes.IMessageProcessor;
import com.firstlinecode.granite.framework.core.pipes.SimpleMessage;
import com.firstlinecode.granite.framework.core.pipes.routing.IPipePostprocessor;
import com.firstlinecode.granite.framework.core.pipes.routing.IProtocolTranslatorFactory;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

public class MinimumRoutingProcessor implements IMessageProcessor, IInitializable,
			IServerConfigurationAware, IApplicationComponentServiceAware {
	private static final Logger logger = LoggerFactory.getLogger(MinimumRoutingProcessor.class);
	
	protected ITranslatingFactory translatingFactory;
	
	private List<IPipePostprocessor> postprocessors;
	private String domain;
	
	private IApplicationComponentService appComponentService;
	
	public MinimumRoutingProcessor() {
		translatingFactory = OxmService.createTranslatingFactory();
		postprocessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		registerPredefinedTranslators();
		loadContributedTranslators();
		loadContributedPostprocessors();
	}
	
	
	@SuppressWarnings("rawtypes")
	private void loadContributedTranslators() {
		List<Class<? extends IProtocolTranslatorFactory>> translatorFactoryClasses =
				appComponentService.getExtensionClasses(IProtocolTranslatorFactory.class);
		if (translatorFactoryClasses == null || translatorFactoryClasses.size() == 0) {
			if (logger.isDebugEnabled())
				logger.debug("No extension which's extension point is {} found.", IProtocolTranslatorFactory.class.getName());
			
			return;
		}
		
		for (Class<? extends IProtocolTranslatorFactory> translatorFactoryClass : translatorFactoryClasses) {
			IProtocolTranslatorFactory<?> translatorFactory = appComponentService.createExtension(translatorFactoryClass);
			translatingFactory.register(translatorFactory.getClass(), createTranslatorFactoryAdapter(translatorFactory));
		}
	}

	private <T> TranslatorFactoryAdapter<T> createTranslatorFactoryAdapter(IProtocolTranslatorFactory<T> translatorFactory) {
		return new TranslatorFactoryAdapter<T>(translatorFactory);
	}
	
	private class TranslatorFactoryAdapter<T> implements ITranslatorFactory<T> {
		private IProtocolTranslatorFactory<T> original;
		
		public TranslatorFactoryAdapter(IProtocolTranslatorFactory<T> original) {
			this.original = original;
		}

		@Override
		public Class<T> getType() {
			return original.getType();
		}

		@Override
		public ITranslator<T> create() {
			return original.createTranslator();
		}
		
	}

	private void loadContributedPostprocessors() {
		List<Class<? extends IPipePostprocessor>> postprocessorClasses = appComponentService.getExtensionClasses(IPipePostprocessor.class);
		if (postprocessorClasses == null || postprocessorClasses.size() == 0) {
			if (logger.isDebugEnabled())
				logger.debug("No extension which's extension point is {} found.", IPipePostprocessor.class.getName());
			
			return;
		}
		
		for (Class<? extends IPipePostprocessor> postprocessorClass : postprocessorClasses) {
			IPipePostprocessor postprocessor = appComponentService.createExtension(postprocessorClass);
			postprocessors.add(postprocessor);
		}
	}

	protected void registerPredefinedTranslators() {
		translatingFactory.register(Iq.class, new IqTranslatorFactory());
		translatingFactory.register(Stream.class, new StreamTranslatorFactory());
		translatingFactory.register(StreamError.class, new StreamErrorTranslatorFactory());
		translatingFactory.register(StanzaError.class, new StanzaErrorTranslatorFactory());
	}
	
	@Override
	public void process(IConnectionContext context, IMessage message) {
		JabberId sessionJid = (JabberId)message.getHeaders().get(IMessage.KEY_SESSION_JID);
		
		try {
			Object payload = message.getPayload();
			
			JabberId target = (JabberId)message.getHeaders().get(IMessage.KEY_MESSAGE_TARGET);
			
			if (target == null && (payload instanceof Stanza)) {
				target = ((Stanza)payload).getTo();
			}
			
			if (target == null) {
				target = sessionJid;
			}
			
			if (domain.equals(target.toString())) {
				throw new RuntimeException("Try to route message to server itself. Maybe a application bug.");
			}
			
			routeToTarget(context, sessionJid, target, message.getPayload());
		} catch (Exception e) {
			routeException(context, sessionJid, e);
		}
	}

	private void routeToTarget(IConnectionContext context, JabberId sessionJid,
			JabberId target, Object out) {
		Map<Object, Object> headers = new HashMap<>();
		headers.put(IMessage.KEY_SESSION_JID, sessionJid);
		headers.put(IMessage.KEY_MESSAGE_TARGET, target);
		
		IMessage message = new SimpleMessage(headers, out);
		
		for (IPipePostprocessor postprocessor : postprocessors) {
			message = postprocessor.beforeRouting(message);
			
			if (message == null) {
				return;
			}
		}
		
		headers = message.getHeaders();
		if (headers.get(IMessage.KEY_SESSION_JID) == null) {
			headers.put(IMessage.KEY_SESSION_JID, sessionJid);
		}
		
		if (headers.get(IMessage.KEY_MESSAGE_TARGET) == null) {
			headers.put(IMessage.KEY_MESSAGE_TARGET, target);
		}
		
		out = message.getPayload();
		if ((out instanceof Stanza) && FlawedProtocolObject.isFlawed(out) &&
				!isFromServer((Stanza)out)) {
			// flawed object maybe misses some information.
			// so we try to use original message.
			String amendedOriginalMessage = getAmendedOriginalMessage((Stanza)out);
			if (amendedOriginalMessage != null)
				out = amendedOriginalMessage;
		}
		
		if (!(out instanceof String)) {
			out = translatingFactory.translate(out);
		}
		
		message = new SimpleMessage(headers, out);
		
		if (logger.isTraceEnabled()) {
			logger.trace("Routing message. Session ID: {}. Target: {}, Message: {}.",
					new Object[] {sessionJid, target, (String)message.getPayload()});
		}
		
		context.write(message);
	}

	private boolean isFromServer(Stanza stanza) {
		if (stanza.getFrom() == null)
			return true;
		
		return domain.equals(stanza.getFrom().toString());
	}
	
	// if original message doesn't set 'from' attribute(it implies stanza is from client send it).
	// we need amend the message to add 'from' attribute.
	private String getAmendedOriginalMessage(Stanza stanza) {
		if (stanza.getOriginalMessage() == null)
			return null;
		
		return getAmendedOriginalXmlMessage(stanza);
	}

	private String getAmendedOriginalXmlMessage(Stanza stanza) {
		String originalMessage = stanza.getOriginalMessage();
		String firstElementStartPart = getXmlMessageFirstElementStartPart(originalMessage);
		if (firstElementStartPart == null)
			return null;
		
		if (firstElementStartPart.indexOf(" from=") != -1)
			return null;
		
		int fromInsertPosition = firstElementStartPart.indexOf(' ');
		if (fromInsertPosition == -1) {
			fromInsertPosition = firstElementStartPart.indexOf('>');
		}
		
		String fromString = String.format(" from=\"%s\"", stanza.getFrom().toString());
		int firstElementStartPartLength = firstElementStartPart.length();
		String newFirstElementStartPart = firstElementStartPart.substring(0, fromInsertPosition)
				+ fromString + firstElementStartPart.substring(fromInsertPosition, firstElementStartPart.length());
		
		return newFirstElementStartPart + originalMessage.substring(firstElementStartPartLength, originalMessage.length());
	}

	private String getXmlMessageFirstElementStartPart(String originalMessage) {
		int elementStartEndIndex = originalMessage.indexOf('>');
		if (elementStartEndIndex == -1) {
			// ??? isn't it a xmpp stanza message?
			return null;
		}
		
		return originalMessage.substring(0, elementStartEndIndex + 1);
	}

	private void routeException(IConnectionContext context, JabberId sessionJid, Exception e) {
		String msgString;
		try {
			if (e instanceof ProtocolException) {
				msgString = translatingFactory.translate(((ProtocolException)e).getError());
			} else {
				msgString = translatingFactory.translate(new InternalServerError(e.getMessage()));
			}
			
			routeToTarget(context, sessionJid, sessionJid, msgString);
		} catch (Exception exception) {
			logger.error("routing error", exception);
		}
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		this.domain = serverConfiguration.getDomainName();
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

}
