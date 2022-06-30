package com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing;

import com.thefirstlineofcode.basalt.oxm.convention.NamingConventionParserFactory;
import com.thefirstlineofcode.basalt.oxm.parsing.IParser;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolChain;

public class NamingConventionProtocolParserFactory<T> implements IProtocolParserFactory<T> {
	private ProtocolChain protocolChain;
	private NamingConventionParserFactory<T> parserFactory;
	
	public NamingConventionProtocolParserFactory(ProtocolChain protocolChain, Class<T> type) {
		this.protocolChain = protocolChain;
		parserFactory = new NamingConventionParserFactory<T>(type);
	}
	
	@Override
	public ProtocolChain getProtocolChain() {
		return protocolChain;
	}
	
	@Override
	public IParser<T> createParser() {
		return parserFactory.create();
	}
}
