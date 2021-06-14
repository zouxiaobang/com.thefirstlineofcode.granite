package com.firstlinecode.granite.framework.core.pipes.processing;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class SingletonXepProcessorFactory<S extends Stanza, X> implements IXepProcessorFactory<S, X> {
	private ProtocolChain protocolChain;
	private IXepProcessor<S, X> processor;
	
	public SingletonXepProcessorFactory(ProtocolChain protocolChain, IXepProcessor<S, X> processor) {
		this.protocolChain = protocolChain;
		this.processor = processor;
	}
	
	@Override
	public ProtocolChain getProtocolChain() {
		return protocolChain;
	}

	@Override
	public IXepProcessor<S, X> createProcessor() {
		return processor;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
