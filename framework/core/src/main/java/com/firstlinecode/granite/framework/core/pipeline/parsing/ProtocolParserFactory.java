package com.firstlinecode.granite.framework.core.pipeline.parsing;

import com.firstlinecode.basalt.oxm.parsing.IParser;
import com.firstlinecode.basalt.oxm.parsing.IParserFactory;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;

public class ProtocolParserFactory<T> implements IProtocolParserFactory<T> {
	private ProtocolChain protocolChain;
	private IParserFactory<T> parserFactory;
	
	public ProtocolParserFactory(ProtocolChain protocolChain, IParserFactory<T> parserFactory) {
		this.protocolChain = protocolChain;
		this.parserFactory = parserFactory;
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
