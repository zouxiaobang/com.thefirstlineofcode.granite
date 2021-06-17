package com.firstlinecode.granite.pipeline.parsing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.parsers.core.stanza.IqParserFactory;
import com.firstlinecode.basalt.oxm.parsers.error.ErrorParserFactory;
import com.firstlinecode.basalt.oxm.parsers.error.StanzaErrorDetailsParserFactory;
import com.firstlinecode.basalt.oxm.parsing.FlawedProtocolObject;
import com.firstlinecode.basalt.oxm.parsing.IParser;
import com.firstlinecode.basalt.oxm.parsing.IParserFactory;
import com.firstlinecode.basalt.oxm.parsing.IParsingFactory;
import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.Protocol;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stream.error.InvalidFrom;
import com.firstlinecode.basalt.protocol.core.stream.error.StreamError;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.pipeline.IMessage;
import com.firstlinecode.granite.framework.core.pipeline.IMessageProcessor;
import com.firstlinecode.granite.framework.core.pipeline.IPipelineExtendersContributor;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IPipelinePreprocessor;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.utils.CommonUtils;

@Component("minimum.message.parsing.processor")
public class MinimumMessageParsingProcessor implements IMessageProcessor, IInitializable,
		IConfigurationAware, IServerConfigurationAware, IApplicationComponentServiceAware {
	private static final String CONFIGURATION_KEY_STANZA_ERROR_ATTACH_SENDER_MESSAGE = "stanza.error.attach.sender.message";
	
	private static final Logger logger = LoggerFactory.getLogger(MinimumMessageParsingProcessor.class);
	
	protected IParsingFactory parsingFactory;
	private boolean stanzaErrorAttachSenderMessage;
	private IServerConfiguration serverConfiguration;
	
	private List<IPipelinePreprocessor> pipesPreprocessors;
	
	private IApplicationComponentService appComponentService;
	
	public MinimumMessageParsingProcessor() {
		parsingFactory = OxmService.createParsingFactory();
		pipesPreprocessors = new CopyOnWriteArrayList<>();
	}
	
	@Override
	public void init() {
		registerPredefinedParsers();
		
		IPipelineExtendersContributor[] extendersFactories = CommonUtils.getExtendersContributors(appComponentService);
		
		loadContributedProtocolParsers(extendersFactories);
		loadContributedPreprocessors(extendersFactories);
	}

	protected void loadContributedPreprocessors(IPipelineExtendersContributor[] extendersContributors) {
		for (IPipelineExtendersContributor extendersContributor : extendersContributors) {
			IPipelinePreprocessor[] preproessors = extendersContributor.getPipesPreprocessors();
			if (preproessors == null || preproessors.length == 0)
				continue;
			
			for (IPipelinePreprocessor preprocessor : preproessors) {
				pipesPreprocessors.add(appComponentService.inject(preprocessor));
				
				if (logger.isDebugEnabled()) {
					logger.debug("Plugin '{}' contributed a pipes preprocessor: '{}'.",
							appComponentService.getPluginManager().whichPlugin(extendersContributor.getClass()),
							preprocessor.getClass().getName()
					);
				}
			}
		}
	}

	protected void loadContributedProtocolParsers(IPipelineExtendersContributor[] extendersContributors) {
		for (IPipelineExtendersContributor extendersContributor : extendersContributors) {
			IProtocolParserFactory<?>[] parserFactories = extendersContributor.getProtocolParserFactories();
			
			if (parserFactories == null || parserFactories.length == 0)
				continue;
			
			for (IProtocolParserFactory<?> parserFactory : parserFactories) {
				parsingFactory.register(parserFactory.getProtocolChain(), createParserFactoryAdapter(parserFactory));
				
				if (logger.isDebugEnabled()) {
					logger.debug("Plugin '{}' contributed a protocol parser factory: '{}'.",
							appComponentService.getPluginManager().whichPlugin(extendersContributor.getClass()),
							parserFactory.getClass().getName()
					);
				}
			}
		}
	}
	
	private <T> IParserFactory<T> createParserFactoryAdapter(IProtocolParserFactory<T> original) {
		return new ParserFactoryAdapter<>(original);
	}

	private class ParserFactoryAdapter<T> implements IParserFactory<T> {
		private IProtocolParserFactory<T> original;
		
		public ParserFactoryAdapter(IProtocolParserFactory<T> original) {
			this.original = original;
		}

		@Override
		public Protocol getProtocol() {
			return original.getProtocolChain().get(original.getProtocolChain().size() - 1);
		}

		@Override
		public IParser<T> create() {
			return original.createParser();
		}
		
	}

	protected void registerPredefinedParsers() {
		parsingFactory.register(
				ProtocolChain.first(Iq.PROTOCOL),
				new IqParserFactory()
		);
		parsingFactory.register(
				ProtocolChain.first(StanzaError.PROTOCOL),
				new ErrorParserFactory<>(IError.Type.STANZA)
		);
		parsingFactory.register(
				ProtocolChain.first(StanzaError.PROTOCOL).next(
						StanzaError.PROTOCOL_ERROR_DEFINED_CONDITION),
				new StanzaErrorDetailsParserFactory()
		);
	}
	
	@Override
	public void process(IConnectionContext context, IMessage message) {
		String msg = (String)message.getPayload();
		
		for (IPipelinePreprocessor preprocessor : pipesPreprocessors) {
			String preprocessedMsg = preprocessor.beforeParsing(msg);
			
			if (preprocessedMsg == null) {
				if (logger.isInfoEnabled())
					logger.info("Message has dropped by preprcessor before parsing. Original message: {}.", msg);
				
				return;
			}
		}
		
		Object out = parseMessage(context, msg);
		
		if (out == null && logger.isWarnEnabled()) {
			logger.warn("Ignored message. Session JID: {}. Message: {}.",
					message.getHeaders().get(IMessage.KEY_SESSION_JID),
					message.getPayload());
			return;
		}
		
		for (IPipelinePreprocessor preprocessor : pipesPreprocessors) {
			Object preprocessedOut = preprocessor.afterParsing(out);
			
			if (preprocessedOut == null) {
				if (logger.isInfoEnabled())
					logger.info("Message object has dropped by preprcessor after parsing. Original object: {}.", msg);
				
				return;
			}
		}
		
		context.write(out);
		
		if (out instanceof StreamError) {
			context.close();
		}
	}

	private Object parseMessage(IConnectionContext context, String message) {
		if (logger.isTraceEnabled())
			logger.trace("Parsing message: {}.", message);
		
		Object out = null;
		try {
			Object object = parsingFactory.parse(message);
			if (object instanceof Stanza) {
				Stanza stanza = (Stanza)object;
				
				if (stanza.getFrom() == null) {
					stanza.setFrom(context.getJid());
				}
				
				// (rfc3920 9.1.2)
				if (!isValidFrom(context, stanza)) {
					throw new ProtocolException(new InvalidFrom());
				}
				
				if (logger.isTraceEnabled())
					logger.trace("Stanza parsed. Original message: {}.", message);
				
				// If server doesn't understand the extended namespaces(rfc3921 2.4)
				if (FlawedProtocolObject.isFlawed(stanza)) {
					if (logger.isTraceEnabled())
						logger.trace("Flawed stanza parsed. Original message: {}.", message);
					
					if (isServerRecipient(stanza)) {
						if (stanza instanceof Iq) {
							Iq iq = (Iq)stanza;
							
							//If an entity receives an IQ stanza of type "get" or "set" containing a child element
							// qualified by a namespace it does not understand, the entity SHOULD return an
							// IQ stanza of type "error" with an error condition of <service-unavailable/>.
							if (iq.getType() == Iq.Type.SET || iq.getType() == Iq.Type.GET) {
								out = stanza;
								throw new ProtocolException(new ServiceUnavailable());
							}
						} else if (stanza instanceof Message) {
							// If an entity receives a message stanza whose only child element is qualified by a
							// namespace it does not understand, it MUST ignore the entire stanza.
							Message messageStanza = (Message)stanza;
							if (messageStanza.getSubjects().size() == 0 &&
									messageStanza.getBodies().size() == 0 &&
									messageStanza.getObjects().size() == 0 &&
									messageStanza.getThread() == null) {
								// Ignore the entire stanza
								return null;
							}
						} else {
							return stanza;
						}
					} else {
						return stanza;
					}					
				}
				
				return object;
			} else {
				// ？？？
				out = object;
			}
		} catch (ProtocolException e) {
			IError error = e.getError();
			if ((error instanceof StanzaError)) {
				if (!stanzaErrorAttachSenderMessage) {
					// Remove sender message
					((StanzaError)error).setOriginalMessage(null);
				}
				
				if (out instanceof Stanza) {
					Stanza stanza = (Stanza)out;
					if (stanza.getId() != null) {
						((StanzaError)error).setId(stanza.getId());
					}
				}
			}
			
			out = error;
			if (logger.isTraceEnabled())
				logger.trace("Parsing protocol exception. original message: {}.", message);
		} catch (RuntimeException e) {
			out = new InternalServerError(CommonUtils.getInternalServerErrorMessage(e));
			logger.error(String.format("Parsing error. original message: %s.", message), e);
		}
		
		return out;
	}

	private boolean isValidFrom(IConnectionContext context, Stanza stanza) {
		if (stanza.getFrom().isBareId() && context.getJid().getBareId().equals(stanza.getFrom()))
			return true;
		
		return stanza.getFrom().equals(context.getJid());
	}

	private boolean isServerRecipient(Stanza stanza) {
		return stanza.getTo() == null || serverConfiguration.getDomainName().equals(stanza.getTo().toString());
	}
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		stanzaErrorAttachSenderMessage = configuration.getBoolean(CONFIGURATION_KEY_STANZA_ERROR_ATTACH_SENDER_MESSAGE, false);
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

}
