package com.firstlinecode.granite.xeps.component.stream.accept.negotiants;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAuthorized;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.Conflict;
import com.firstlinecode.basalt.protocol.core.stream.error.InternalServerError;
import com.firstlinecode.basalt.protocol.oxm.IOxmFactory;
import com.firstlinecode.basalt.protocol.oxm.OxmService;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManager;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.stream.StreamConstants;
import com.firstlinecode.granite.framework.stream.negotiants.AbstractNegotiant;
import com.firstlinecode.granite.xeps.component.stream.IComponentConnectionsRegister;
import com.firstlinecode.granite.xeps.component.stream.accept.ComponentConstants;

public class HandshakeNegotiant extends AbstractNegotiant {
	private static final IOxmFactory oxmFactory = OxmService.createMinimumOxmFactory();
	
	private static final String MESSAGE_CONFLICT = oxmFactory.translate(new Conflict());
	private static final String MESSAGE_CLOSE_STREAM = oxmFactory.translate(new Stream(true));
	
	private Map<String, String> components;
	private IConnectionManager connectionManager;
	private IComponentConnectionsRegister connectionsRegister;
	
	public HandshakeNegotiant(Map<String, String> components, IConnectionManager connectionManager, IComponentConnectionsRegister connectionsRegister) {
		this.components = components;
		this.connectionManager = connectionManager;
		this.connectionsRegister = connectionsRegister;
	}

	@Override
	protected boolean doNegotiate(IClientConnectionContext context, IMessage message) {
		String componentName = context.removeAttribute(ComponentConstants.SESSION_KEY_COMPONENT_NAME);
		String sid = context.removeAttribute(ComponentConstants.SESSION_KEY_COMPONENT_STREAM_ID);
		
		if (componentName == null) {
			throw new ProtocolException(new InternalServerError("null component name"));
		}
		
		if (sid == null) {
			throw new ProtocolException(new InternalServerError("null stream id"));
		}
		
		String secret = components.get(componentName);
		if (secret == null) {
			throw new ProtocolException(new InternalServerError("null secret"));
		}
		
		String sidAndSecret = sid + secret;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] hash = digest.digest(sidAndSecret.getBytes());
			
			String credentials = new BigInteger(1, hash).toString(16);
			if (validate(credentials, message.getPayload())) {
				removeOldComponentConnectionIfExists(componentName);
				
				connectionsRegister.register(componentName, context.getConnectionId());
				
				context.setAttribute(StreamConstants.KEY_SESSION_JID, JabberId.parse(componentName));
				context.write("<handshake/>");
				return true;
			}
			
			throw new ProtocolException(new NotAuthorized());
		} catch (NoSuchAlgorithmException e) {
			throw new ProtocolException(new InternalServerError(), e);
		}		
	}

	private void removeOldComponentConnectionIfExists(String componentName) {
		if (connectionsRegister.getConnectionId(componentName) != null) {
			connectionsRegister.getConnectionId(componentName);
			
			IClientConnectionContext oldContext = (IClientConnectionContext)connectionManager.
					getConnectionContext(JabberId.parse(componentName));
			if (oldContext != null) {
				oldContext.write(MESSAGE_CONFLICT, true);
				oldContext.write(MESSAGE_CLOSE_STREAM, true);
				
				oldContext.close(true);
			}
			
			connectionsRegister.unregister(componentName);
		}
	}

	private boolean validate(String credentials, Object requestCredentials) {
		return String.format("<handshake>%s</handshake>", credentials).equals(requestCredentials);
	}

}
