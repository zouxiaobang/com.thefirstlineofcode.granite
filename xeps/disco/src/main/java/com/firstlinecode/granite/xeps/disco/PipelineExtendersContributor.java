package com.firstlinecode.granite.xeps.disco;

import org.pf4j.Extension;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.parsing.NamingConventionProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.NamingConventionProtocolTranslatorFactory;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	
	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return new IXepProcessorFactory<?, ?>[] {
			new SingletonXepProcessorFactory<>(
					ProtocolChain.first(Iq.PROTOCOL).next(DiscoInfo.PROTOCOL),
					new DiscoInfoProcessor()),
			new SingletonXepProcessorFactory<>(
					ProtocolChain.first(Iq.PROTOCOL).next(DiscoItems.PROTOCOL),
					new DiscoItemsProcessor())
		};
	}
	
	@Override
	public IProtocolParserFactory<?>[] getProtocolParserFactories() {
		return new IProtocolParserFactory<?>[] {
			new NamingConventionProtocolParserFactory<>(
					ProtocolChain.first(Iq.PROTOCOL).next(DiscoInfo.PROTOCOL),
					DiscoInfo.class),
			new NamingConventionProtocolParserFactory<>(
					ProtocolChain.first(Iq.PROTOCOL).next(DiscoItems.PROTOCOL),
					DiscoItems.class)
		};
	}
	
	@Override
	public IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories() {
		return new IProtocolTranslatorFactory<?>[] {
			new NamingConventionProtocolTranslatorFactory<>(DiscoInfo.class),
			new NamingConventionProtocolTranslatorFactory<>(DiscoItems.class)
		};
	}
}
