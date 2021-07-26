package com.firstlinecode.granite.xeps.disco;

import org.pf4j.Extension;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.granite.framework.core.pipeline.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.SingletonXepProcessorFactory;

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

}
