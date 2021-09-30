package com.thefirstlineofcode.granite.xeps.rsm;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xeps.rsm.Set;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			Set.class
		};
	}
}
