package com.firstlinecode.granite.pipeline.stages.parsing;

import com.firstlinecode.basalt.oxm.parsers.im.MessageParserFactory;
import com.firstlinecode.basalt.oxm.parsers.im.PresenceParserFactory;
import com.firstlinecode.basalt.protocol.core.MessageProtocolChain;
import com.firstlinecode.basalt.protocol.core.PresenceProtocolChain;
import com.firstlinecode.granite.framework.core.annotations.Component;

@Component("default.message.parsing.processor")
public class DefaultMessageParsingProcessor extends MinimumMessageParsingProcessor {
	@Override
	protected void registerPredefinedParsers() {
		super.registerPredefinedParsers();
		
		parsingFactory.register(new PresenceProtocolChain(), new PresenceParserFactory());
		parsingFactory.register(new MessageProtocolChain(), new MessageParserFactory());
	}
	
}
