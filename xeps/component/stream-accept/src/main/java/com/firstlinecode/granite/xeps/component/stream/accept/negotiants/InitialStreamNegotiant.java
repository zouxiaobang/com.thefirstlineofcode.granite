package com.firstlinecode.granite.xeps.component.stream.accept.negotiants;

import java.util.Set;
import java.util.UUID;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.HostUnknown;
import com.firstlinecode.basalt.protocol.core.stream.error.InvalidNamespace;
import com.firstlinecode.basalt.protocol.core.stream.error.NotAuthorized;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.stream.negotiants.AbstractNegotiant;
import com.firstlinecode.granite.xeps.component.stream.accept.ComponentConstants;

public class InitialStreamNegotiant extends AbstractNegotiant {
	private static IOxmFactory oxmFactory = OxmService.createStreamOxmFactory();
	private static final String NAMESPACE_JABBER_COMPONENT_ACCEPT = "jabber:component:accept";
	
	private Set<String> components;
	
	public InitialStreamNegotiant(Set<String> components) {
		if (components == null) {
			throw new IllegalArgumentException("null components");
		}
		
		this.components = components;
	}
	
	@Override
	protected boolean doNegotiate(IClientConnectionContext context, IMessage message) {
		Object obj = oxmFactory.parse((String)message.getPayload(), true);
		
		if (obj instanceof Stream) {
			Stream initialStream = (Stream)obj;
			
			if (!NAMESPACE_JABBER_COMPONENT_ACCEPT.equals(initialStream.getDefaultNamespace())) {
				throw new ProtocolException(new InvalidNamespace());
			}
			
			JabberId componentJid = initialStream.getTo();
			if (componentJid == null || !componentJid.isBareId() || componentJid.getNode() != null) {
				throw new ProtocolException(new HostUnknown());
			}
			
			if (!isServiced(componentJid.getDomain())) {
				throw new ProtocolException(new HostUnknown());
			}
			
			context.setAttribute(ComponentConstants.SESSION_KEY_COMPONENT_NAME, componentJid.getDomain());
			
			Stream openStream = new Stream();
			openStream.setFrom(componentJid);
			
			openStream.setDefaultNamespace(NAMESPACE_JABBER_COMPONENT_ACCEPT);
			
			String id = Long.toHexString(UUID.randomUUID().getLeastSignificantBits()).substring(4, 12);
			openStream.setId(id);
			context.setAttribute(ComponentConstants.SESSION_KEY_COMPONENT_STREAM_ID, id);
			
			context.write(oxmFactory.translate(openStream), true);
			
			return true;
		} else {
			throw new ProtocolException(new NotAuthorized());
		}
	}
	
	private boolean isServiced(String component) {
		return components.contains(component);
	}

	protected String getDefaultNamespace() {
		return Constants.C2S_DEFAULT_NAMESPACE;
	}
}
