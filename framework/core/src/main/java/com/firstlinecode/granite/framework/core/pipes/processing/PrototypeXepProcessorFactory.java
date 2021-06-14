package com.firstlinecode.granite.framework.core.pipes.processing;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class PrototypeXepProcessorFactory<S extends Stanza, X> implements IXepProcessorFactory<S, X> {
	private ProtocolChain protocolChain;
	private Class<IXepProcessor<S, X>> processorClass;
	
	public PrototypeXepProcessorFactory(ProtocolChain protocolChain, Class<IXepProcessor<S, X>> processorClass) {
		this.protocolChain = protocolChain;
		this.processorClass = processorClass;
	}
	
	@Override
	public ProtocolChain getProtocolChain() {
		return protocolChain;
	}

	@Override
	public IXepProcessor<S, X> createProcessor() throws Exception {
		return processorClass.newInstance();
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
