package com.firstlinecode.granite.framework.parsing;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.basalt.oxm.parsing.IParser;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;

public interface IProtocolParserFactory<T> extends ExtensionPoint {
	ProtocolChain getProtocolChain();
	IParser<T> createParser();
}
