package com.firstlinecode.granite.pipeline.stages.stream.negotiants;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.parsers.SimpleObjectParserFactory;
import com.firstlinecode.basalt.oxm.parsers.core.stanza.IqParserFactory;
import com.firstlinecode.basalt.oxm.translators.core.stanza.IqTranslatorFactory;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.Conflict;
import com.firstlinecode.basalt.protocol.core.stanza.error.Forbidden;
import com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAuthorized;
import com.firstlinecode.basalt.protocol.core.stream.Session;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.pipeline.IMessage;
import com.firstlinecode.granite.framework.core.pipeline.IMessageChannel;
import com.firstlinecode.granite.framework.core.pipeline.SimpleMessage;
import com.firstlinecode.granite.framework.core.pipeline.stages.event.SessionEstablishedEvent;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.IRouter;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.RoutingRegistrationException;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.framework.core.session.ISessionListener;
import com.firstlinecode.granite.framework.core.session.ISessionManager;
import com.firstlinecode.granite.framework.core.session.SessionExistsException;
import com.firstlinecode.granite.pipeline.stages.stream.StreamConstants;

public class SessionEstablishmentNegotiant extends AbstractNegotiant {
	private static final Logger logger = LoggerFactory.getLogger(SessionEstablishmentNegotiant.class);
	
	private static IOxmFactory oxmFactory = OxmService.createStreamOxmFactory();
	
	static {
		oxmFactory.register(ProtocolChain.first(Iq.PROTOCOL),
				new IqParserFactory()
		);
		oxmFactory.register(ProtocolChain.first(Iq.PROTOCOL).next(Session.PROTOCOL),
				new SimpleObjectParserFactory<>(
						Session.PROTOCOL,
						Session.class
				)
		);
		
		oxmFactory.register(Iq.class, new IqTranslatorFactory());
	}
	
	private IRouter router;
	private ISessionManager sessionManager;
	private IMessageChannel eventMessageChannel;
	private ISessionListener sessionListener;
	
	public SessionEstablishmentNegotiant(IRouter router, ISessionManager sessionManager,
			IMessageChannel eventMessageChannel, ISessionListener sessionListener) {
		this.sessionManager = sessionManager;
		this.router = router;
		this.eventMessageChannel = eventMessageChannel;
		this.sessionListener = sessionListener;
	}
	
	@Override
	protected boolean doNegotiate(IClientConnectionContext context, IMessage message) {
		Iq request = (Iq)oxmFactory.parse((String)message.getPayload());
		
		if (request.getObject() instanceof Session) {
			JabberId sessionJid = context.removeAttribute(StreamConstants.KEY_BINDED_JID);
			if (sessionJid == null) {
				throw new ProtocolException(new Forbidden());
			}
			
			try {
				sessionListener.sessionEstablishing(context, sessionJid);
			} catch (Exception e) {
				logger.error(String.format("Failed to call sessionEstablishing() of session listeners. JID: %s.",
						sessionJid), e);
				throw new ProtocolException(new InternalServerError(e.getMessage()));
			}
			
			
			ISession session = null;
			try {
				session = sessionManager.create(sessionJid);
			} catch (SessionExistsException e) {
				// TODO Maybe we should remove previous session and disconnect the associated client.
				throw new ProtocolException(new Conflict(String.format("Session '%s' has already existed.")));
			}
			
			context.setAttribute(StreamConstants.KEY_SESSION_JID, sessionJid);
			session.setAttribute(StreamConstants.KEY_CLIENT_SESSION_ID,
					context.getConnectionId());
			session.setAttribute(ISession.KEY_SESSION_JID, sessionJid);
			
			sessionManager.put(sessionJid, session);
			
			try {
				router.register(sessionJid, context.getLocalNodeId());
			} catch (RoutingRegistrationException e) {
				logger.error(String.format("Can't register to router. JID: %s.", sessionJid),
						e);
				sessionManager.remove(sessionJid);
				throw new ProtocolException(new InternalServerError(e.getMessage()));
			}
			
			try {
				sessionListener.sessionEstablished(context, sessionJid);
			} catch (Exception e) {
				logger.error(String.format("Failed to call sessionEstablished() of session listeners. JID: %s.",
						sessionJid), e);
				try {
					router.unregister(sessionJid);
				} catch (RoutingRegistrationException e1) {
					logger.error("Can't unregister from router. JID: {}.", sessionJid);
				}
				sessionManager.remove(sessionJid);
				
				throw new ProtocolException(new InternalServerError(e.getMessage()));
			}
			
			
			fireSessionEstablishedEvent(context, sessionJid);
			
			Iq response = new Iq(Iq.Type.RESULT);
			response.setId(request.getId());
			
			context.write(oxmFactory.translate(response));
			
			return true;
		} else {
			throw new ProtocolException(new NotAuthorized());
		}
	}
	
	private void fireSessionEstablishedEvent(IConnectionContext context, JabberId jid) {
		SessionEstablishedEvent event = new SessionEstablishedEvent(context.getJid().toString(), jid);
		
		Map<Object, Object> headers = new HashMap<>();
		headers.put(IMessage.KEY_SESSION_JID, jid);
		
		eventMessageChannel.send(new SimpleMessage(headers, event));
	}

}
