package com.firstlinecode.granite.xeps.component.parsing;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.protocol.oxm.parsers.im.MessageParserFactory;
import com.firstlinecode.basalt.protocol.oxm.parsers.im.PresenceParserFactory;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.parsing.MinimumMessageParsingProcessor;

@Component("default.component.message.parsing.processor")
public class DefaultMessageParsingProcessor extends MinimumMessageParsingProcessor {
	
	public DefaultMessageParsingProcessor() {
		super("Basalt-Component-Parsers", "Granite-Component-Pipe-Preprocessors");
	}
	
	@Override
	protected void registerPredefinedParsers() {
		super.registerPredefinedParsers();
		
		parsingFactory.register(ProtocolChain.first(Presence.PROTOCOL), new PresenceParserFactory());
		parsingFactory.register(ProtocolChain.first(Message.PROTOCOL), new MessageParserFactory());
	}
	
}
