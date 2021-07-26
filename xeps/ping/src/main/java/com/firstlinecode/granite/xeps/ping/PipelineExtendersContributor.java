package com.firstlinecode.granite.xeps.ping;

import org.pf4j.Extension;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.ping.Ping;
import com.firstlinecode.granite.framework.core.pipeline.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.parsing.SimpleObjectProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.SingletonXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.routing.IProtocolTranslatorFactory;
import com.firstlinecode.granite.framework.core.pipeline.routing.SimpleObjectProtocolTranslatorFactory;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	private static final ProtocolChain PROTOCOL_CHAIN = ProtocolChain.first(Iq.PROTOCOL).next(Ping.PROTOCOL);
	
	@Override
	public IProtocolParserFactory<?>[] getProtocolParserFactories() {
		return new IProtocolParserFactory<?>[] {
			new SimpleObjectProtocolParserFactory<Ping>(PROTOCOL_CHAIN, Ping.class)
		};
	}
	
	@Override
	public IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories() {
		return new IProtocolTranslatorFactory<?>[] {
			new SimpleObjectProtocolTranslatorFactory<Ping>(Ping.class, Ping.PROTOCOL)
		};
	}
	
	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return new IXepProcessorFactory<?, ?>[] {
			new SingletonXepProcessorFactory<Iq, Ping>(PROTOCOL_CHAIN, new PingProcessor())
		};
	}

}
