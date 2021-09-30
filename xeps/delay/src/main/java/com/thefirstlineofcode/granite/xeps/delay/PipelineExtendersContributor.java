package com.thefirstlineofcode.granite.xeps.delay;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xeps.delay.Delay;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			Delay.class
		};
	}
}
