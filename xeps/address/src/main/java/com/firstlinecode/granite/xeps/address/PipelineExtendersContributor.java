package com.firstlinecode.granite.xeps.address;

import org.pf4j.Extension;

import com.firstlinecode.basalt.xeps.address.Addresses;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			Addresses.class
		};
	}
}
