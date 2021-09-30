package com.thefirstlineofcode.granite.xeps.address;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xeps.address.Addresses;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			Addresses.class
		};
	}
}
