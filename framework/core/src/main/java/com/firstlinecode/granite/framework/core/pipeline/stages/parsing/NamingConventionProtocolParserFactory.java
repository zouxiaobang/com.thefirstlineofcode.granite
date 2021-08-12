package com.firstlinecode.granite.framework.core.pipeline.stages.parsing;

import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.oxm.parsing.IParser;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;

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
