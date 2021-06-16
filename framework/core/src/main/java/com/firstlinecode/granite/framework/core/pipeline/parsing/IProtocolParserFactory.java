package com.firstlinecode.granite.framework.core.pipeline.parsing;

import com.firstlinecode.basalt.oxm.parsing.IParser;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;

public interface IProtocolParserFactory<T> {
	ProtocolChain getProtocolChain();
	IParser<T> createParser();
}
