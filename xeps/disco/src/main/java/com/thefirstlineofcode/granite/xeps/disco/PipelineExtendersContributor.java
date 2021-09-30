package com.thefirstlineofcode.granite.xeps.disco;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.protocol.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xeps.disco.DiscoInfo;
import com.thefirstlineofcode.basalt.xeps.disco.DiscoItems;
import com.thefirstlineofcode.basalt.xeps.rsm.Set;
import com.thefirstlineofcode.basalt.xeps.xdata.XData;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;

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
