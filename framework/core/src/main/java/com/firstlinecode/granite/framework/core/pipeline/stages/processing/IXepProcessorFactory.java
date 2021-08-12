package com.firstlinecode.granite.framework.core.pipeline.stages.processing;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IXepProcessorFactory<S extends Stanza, X> {
	ProtocolChain getProtocolChain();
	IXepProcessor<S, X> createProcessor() throws Exception;
	boolean isSingleton();
}
