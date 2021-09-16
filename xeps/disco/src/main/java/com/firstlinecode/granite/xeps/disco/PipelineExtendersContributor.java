package com.firstlinecode.granite.xeps.disco;

import org.pf4j.Extension;

import com.firstlinecode.basalt.protocol.core.IqProtocolChain;
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
					new IqProtocolChain(DiscoInfo.PROTOCOL),
					new DiscoInfoProcessor()),
			new SingletonXepProcessorFactory<>(
					new IqProtocolChain(DiscoItems.PROTOCOL),
					new DiscoItemsProcessor())
		};
	}
	
	@Override
	protected NamingConventionParsableProtocolObject[] getNamingConventionParsableProtocolObjects() {
		return new NamingConventionParsableProtocolObject[] {
				new NamingConventionParsableProtocolObject(
						new IqProtocolChain(DiscoInfo.PROTOCOL),
						DiscoInfo.class),
				new NamingConventionParsableProtocolObject(
						new IqProtocolChain().next(DiscoInfo.PROTOCOL).next(XData.PROTOCOL),
						XData.class),
				new NamingConventionParsableProtocolObject(
						new IqProtocolChain(DiscoItems.PROTOCOL),
						DiscoItems.class),
				new NamingConventionParsableProtocolObject(
						new IqProtocolChain().next(DiscoItems.PROTOCOL).next(Set.PROTOCOL),
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
