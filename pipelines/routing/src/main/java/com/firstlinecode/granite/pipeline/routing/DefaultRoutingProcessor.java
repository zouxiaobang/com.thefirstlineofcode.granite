package com.firstlinecode.granite.pipeline.routing;

import com.firstlinecode.basalt.oxm.translators.im.MessageTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.im.PresenceTranslatorFactory;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.annotations.Component;

@Component("default.routing.processor")
public class DefaultRoutingProcessor extends MinimumRoutingProcessor {
	
	@Override
	protected void registerPredefinedTranslators() {
		super.registerPredefinedTranslators();
		
		translatingFactory.register(Presence.class, new PresenceTranslatorFactory());
		translatingFactory.register(Message.class, new MessageTranslatorFactory());
	}
}
