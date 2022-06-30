package com.thefirstlineofcode.granite.xeps.disco;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xeps.disco.DiscoInfo;
import com.thefirstlineofcode.basalt.xeps.disco.DiscoItems;
import com.thefirstlineofcode.basalt.xeps.rsm.Set;
import com.thefirstlineofcode.basalt.xeps.xdata.XData;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.
			registerNamingConventionParser(new IqProtocolChain(DiscoInfo.PROTOCOL),
				DiscoInfo.class).
			registerNamingConventionParser(new IqProtocolChain(DiscoInfo.PROTOCOL).next(XData.PROTOCOL),
				XData.class).
			registerNamingConventionParser(new IqProtocolChain(DiscoItems.PROTOCOL),
				DiscoItems.class).
			registerNamingConventionParser(new IqProtocolChain(DiscoItems.PROTOCOL).next(Set.PROTOCOL),
				Set.class);
			
		configurator.
			registerSingletonXepProcessor(new IqProtocolChain(DiscoInfo.PROTOCOL), new DiscoInfoProcessor()).
			registerSingletonXepProcessor(new IqProtocolChain(DiscoItems.PROTOCOL), new DiscoItemsProcessor());
		
		configurator.
			registerNamingConventionTranslator(DiscoInfo.class).
			registerNamingConventionTranslator(DiscoItems.class);
	}
}
