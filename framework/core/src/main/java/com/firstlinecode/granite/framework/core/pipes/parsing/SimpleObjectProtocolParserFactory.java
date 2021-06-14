package com.firstlinecode.granite.framework.core.pipes.parsing;

import com.firstlinecode.basalt.oxm.parsers.SimpleObjectParserFactory;
import com.firstlinecode.basalt.oxm.parsing.IParser;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;

public class SimpleObjectProtocolParserFactory<T> implements IProtocolParserFactory<T> {
	private ProtocolChain protocolChain;
	private SimpleObjectParserFactory<T> parserFactory;
	
	public SimpleObjectProtocolParserFactory(ProtocolChain protocolChain, Class<T> type) {
		this.protocolChain = protocolChain;
		parserFactory = new SimpleObjectParserFactory<>(protocolChain.get(protocolChain.size() - 1), type);
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
