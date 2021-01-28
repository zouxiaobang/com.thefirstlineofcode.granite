package com.firstlinecode.granite.xeps.component.routing;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.oxm.translators.im.MessageTranslatorFactory;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.routing.MinimumRoutingProcessor;

@Component("default.component.routing.processor")
public class DefaultRoutingProcessor extends MinimumRoutingProcessor {
	
	public DefaultRoutingProcessor() {
		super("Basalt-Component-Translators", "Granite-Component-Pipe-Postprocessor");
	}
	
	@Override
	protected void registerPredefinedTranslators() {
		super.registerPredefinedTranslators();
		
		translatingFactory.register(Message.class, new MessageTranslatorFactory());
	}
}
