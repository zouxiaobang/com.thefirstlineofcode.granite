package com.firstlinecode.granite.framework.routing;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.protocol.oxm.translators.im.MessageTranslatorFactory;
import com.firstlinecode.basalt.protocol.oxm.translators.im.PresenceTranslatorFactory;
import com.firstlinecode.granite.framework.core.annotations.Component;

@Component("default.routing.processor")
public class DefaultRoutingProcessor extends MinimumRoutingProcessor {
	
	public DefaultRoutingProcessor() {
		super("Basalt-Translators", "Granite-Pipe-Postprocessors");
	}
	
	@Override
	protected void registerPredefinedTranslators() {
		super.registerPredefinedTranslators();
		
		translatingFactory.register(Presence.class, new PresenceTranslatorFactory());
		translatingFactory.register(Message.class, new MessageTranslatorFactory());
	}
}
