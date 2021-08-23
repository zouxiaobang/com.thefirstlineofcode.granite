package com.firstlinecode.granite.xeps.rsm;

import org.pf4j.Extension;

import com.firstlinecode.basalt.xeps.rsm.Set;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			Set.class
		};
	}
}
