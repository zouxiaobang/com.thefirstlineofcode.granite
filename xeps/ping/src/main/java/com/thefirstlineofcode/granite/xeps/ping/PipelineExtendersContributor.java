package com.thefirstlineofcode.granite.xeps.ping;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.protocol.core.ProtocolChain;
import com.thefirstlineofcode.basalt.protocol.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xeps.ping.Ping;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.SimpleObjectProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.SimpleObjectProtocolTranslatorFactory;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	private static final ProtocolChain PROTOCOL_CHAIN = ProtocolChain.first(Iq.PROTOCOL).next(Ping.PROTOCOL);
	
	@Override
	protected IProtocolParserFactory<?>[] getCustomizedParserFactories() {
		return new IProtocolParserFactory<?>[] {
			new SimpleObjectProtocolParserFactory<Ping>(PROTOCOL_CHAIN, Ping.class)
		};
	}
	
	@Override
	protected IProtocolTranslatorFactory<?>[] getCustomizedTranslatorFactories() {
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
