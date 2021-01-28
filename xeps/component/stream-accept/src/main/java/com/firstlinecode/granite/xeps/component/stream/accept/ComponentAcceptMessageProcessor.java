package com.firstlinecode.granite.xeps.component.stream.accept;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.LangText;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stream.error.StreamError;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.annotation.AnnotatedParserFactory;
import com.firstlinecode.basalt.oxm.parsers.core.stream.StreamParser;
import com.firstlinecode.basalt.oxm.parsing.IParsingFactory;
import com.firstlinecode.basalt.oxm.translating.ITranslatingFactory;
import com.firstlinecode.basalt.oxm.translators.core.stream.StreamTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.error.StanzaErrorTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.error.StreamErrorTranslatorFactory;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.commons.utils.CommonUtils;
import com.firstlinecode.granite.framework.core.commons.utils.IoUtils;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManager;
import com.firstlinecode.granite.framework.core.connection.IConnectionManagerAware;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;
import com.firstlinecode.granite.framework.stream.IStreamNegotiant;
import com.firstlinecode.granite.framework.stream.StreamConstants;
import com.firstlinecode.granite.xeps.component.stream.IComponentConnectionsRegister;
import com.firstlinecode.granite.xeps.component.stream.IComponentMessageProcessor;
import com.firstlinecode.granite.xeps.component.stream.accept.negotiants.HandshakeNegotiant;
import com.firstlinecode.granite.xeps.component.stream.accept.negotiants.InitialStreamNegotiant;

@Component("component.accept.message.processor")
public class ComponentAcceptMessageProcessor implements IComponentMessageProcessor,
		IApplicationConfigurationAware, IConnectionManagerAware {
	private static final Logger logger = LoggerFactory.getLogger(ComponentAcceptMessageProcessor.class);
	
	private static final Object KEY_NEGOTIANT = "granite.key.negotiant";
	
	private IParsingFactory parsingFactory;
	private ITranslatingFactory translatingFactory;
	
	protected IMessageChannel messageChannel;
	
	private Map<String, String> components = new HashMap<>();
	
	private IComponentConnectionsRegister connectionsRegister;
	
	private IConnectionManager connectionManager;
	
	public ComponentAcceptMessageProcessor() {
		parsingFactory = OxmService.createParsingFactory();
		parsingFactory.register(ProtocolChain.first(Stream.PROTOCOL), new AnnotatedParserFactory<>(StreamParser.class));
		
		translatingFactory = OxmService.createTranslatingFactory();
		translatingFactory.register(Stream.class, new StreamTranslatorFactory());
		translatingFactory.register(StreamError.class, new StreamErrorTranslatorFactory());
		translatingFactory.register(StanzaError.class, new StanzaErrorTranslatorFactory());
	}

	@Override
	public void process(IConnectionContext context, IMessage message) {
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
			IStreamNegotiant negotiant = (IStreamNegotiant)context.getAttribute(KEY_NEGOTIANT);
			if (negotiant == null) {
				negotiant = createNegotiant();
				context.setAttribute(KEY_NEGOTIANT, negotiant);
			}
			
			try {
				if (negotiant.negotiate((IClientConnectionContext)context, message)) {
					context.removeAttribute(KEY_NEGOTIANT);
				}
			} catch (ProtocolException e) {
				context.write(translatingFactory.translate(e.getError()));
				closeStream(context);
			} catch (RuntimeException e) {
				logger.warn("negotiation error", e);
				
				InternalServerError error = new InternalServerError();
				error.setText(new LangText(String.format("negotiation error. %s",
						CommonUtils.getInternalServerErrorMessage(e))));
				context.write(translatingFactory.translate(error));
				closeStream(context);
			}
			
		}
	}
	
	private IStreamNegotiant createNegotiant() {
		IStreamNegotiant initialStreamNegotiant = new InitialStreamNegotiant(components.keySet());
		initialStreamNegotiant.setNext(new HandshakeNegotiant(components, connectionManager, connectionsRegister));
		
		return initialStreamNegotiant;
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
	
	@Dependency("message.channel")
	public void setMessageChannel(IMessageChannel messageChannel) {
		this.messageChannel = messageChannel;
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		File config = new File(appConfiguration.getConfigDir(), "jabber-component-protocol.ini");
		if (!config.exists()) {
			logger.warn("no jabber component configured. please define your jabber-component-protocol.ini file");
			return;
		}
		
		Properties properties = new Properties();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(config));
			properties.load(reader);
		} catch (Exception e) {
			throw new RuntimeException("can't reader jabber-component-protocol.ini", e);
		} finally {
			IoUtils.closeIO(reader);
		}
		
		for (Object component : properties.keySet()) {
			components.put((String)component, (String)properties.getProperty((String)component));
		}
	}

	@Override
	public void setComponentConnectionsRegister(IComponentConnectionsRegister connectionsRegister) {
		this.connectionsRegister = connectionsRegister;
	}

	@Override
	public void setConnectionManager(IConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
}
