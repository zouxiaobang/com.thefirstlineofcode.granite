package com.firstlinecode.granite.xeps.ping;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.ping.Ping;
import com.firstlinecode.granite.framework.core.pipes.PipesExtendersFactoryAdapter;
import com.firstlinecode.granite.framework.core.pipes.processing.IXepProcessor;
import com.firstlinecode.granite.framework.core.pipes.processing.IXepProcessorFactory;

public class PipeExtendersFactory extends PipesExtendersFactoryAdapter {
	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return new IXepProcessorFactory<?, ?>[] {
			new PingXepProcessorFactory()
		};
	}
	
	private static class PingXepProcessorFactory implements IXepProcessorFactory<Iq, Ping> {	
		private static final ProtocolChain PROTOCOL_CHAIN = ProtocolChain.first(Iq.PROTOCOL).next(Ping.PROTOCOL);
		
		@Override
		public ProtocolChain getProtocolChain() {
			return PROTOCOL_CHAIN;
		}

		@Override
		public IXepProcessor<Iq, Ping> createProcessor() {
			return new PingProcessor();
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

	}

}
