package com.firstlinecode.granite.xeps.delay;

import org.pf4j.Extension;

import com.firstlinecode.basalt.xeps.delay.Delay;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			Delay.class
		};
	}
}
