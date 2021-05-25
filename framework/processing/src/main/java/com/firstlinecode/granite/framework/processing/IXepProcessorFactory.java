package com.firstlinecode.granite.framework.processing;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IXepProcessorFactory<S extends Stanza, X> extends ExtensionPoint {
	ProtocolChain getProtocolChain();
	IXepProcessor<S, X> createProcessor();
	boolean isSingleton();
}
