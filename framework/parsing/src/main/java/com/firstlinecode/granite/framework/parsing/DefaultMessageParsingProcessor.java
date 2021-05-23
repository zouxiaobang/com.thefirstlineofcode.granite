package com.firstlinecode.granite.framework.parsing;

import com.firstlinecode.granite.framework.core.annotations.Component;

@Component("default.message.parsing.processor")
public class DefaultMessageParsingProcessor extends MinimumMessageParsingProcessor {
	
	/*public DefaultMessageParsingProcessor() {
		super("Basalt-Parsers", "Granite-Pipe-Preprocessors");
	}
	
	@Override
	protected void registerPredefinedParsers() {
		super.registerPredefinedParsers();
		
		parsingFactory.register(ProtocolChain.first(Presence.PROTOCOL), new PresenceParserFactory());
		parsingFactory.register(ProtocolChain.first(Message.PROTOCOL), new MessageParserFactory());
	}*/
	
}
