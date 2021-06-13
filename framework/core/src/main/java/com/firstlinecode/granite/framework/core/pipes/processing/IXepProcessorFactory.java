package com.firstlinecode.granite.framework.core.pipes.processing;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IXepProcessorFactory<S extends Stanza, X> {
	ProtocolChain getProtocolChain();
	IXepProcessor<S, X> createProcessor();
	boolean isSingleton();
}
