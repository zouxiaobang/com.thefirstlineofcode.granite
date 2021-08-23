package com.firstlinecode.granite.xeps.disco;

import org.pf4j.Extension;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.rsm.Set;
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;

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
	protected NamingConventionParsableProtocolObject[] getNamingConventionParsableProtocolObjects() {
		return new NamingConventionParsableProtocolObject[] {
				new NamingConventionParsableProtocolObject(
						ProtocolChain.first(Iq.PROTOCOL).next(DiscoInfo.PROTOCOL),
						DiscoInfo.class),
				new NamingConventionParsableProtocolObject(
						ProtocolChain.first(Iq.PROTOCOL).next(DiscoInfo.PROTOCOL).next(XData.PROTOCOL),
						XData.class),
				new NamingConventionParsableProtocolObject(
						ProtocolChain.first(Iq.PROTOCOL).next(DiscoItems.PROTOCOL),
						DiscoItems.class),
				new NamingConventionParsableProtocolObject(
						ProtocolChain.first(Iq.PROTOCOL).next(DiscoItems.PROTOCOL).next(Set.PROTOCOL),
						Set.class)
		};
	}
	
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			DiscoInfo.class,
			DiscoItems.class
		};
	}
}
