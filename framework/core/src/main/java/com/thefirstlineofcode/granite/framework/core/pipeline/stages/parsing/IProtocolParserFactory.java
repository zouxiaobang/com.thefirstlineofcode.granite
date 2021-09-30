package com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing;

import com.thefirstlineofcode.basalt.oxm.parsing.IParser;
import com.thefirstlineofcode.basalt.protocol.core.ProtocolChain;

public interface IProtocolParserFactory<T> {
	ProtocolChain getProtocolChain();
	IParser<T> createParser();
}
