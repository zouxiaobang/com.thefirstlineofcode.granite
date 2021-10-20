package com.thefirstlineofcode.granite.xeps.xdata;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xeps.xdata.XData;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.registerNamingConventionTranslator(XData.class);
	}
}
