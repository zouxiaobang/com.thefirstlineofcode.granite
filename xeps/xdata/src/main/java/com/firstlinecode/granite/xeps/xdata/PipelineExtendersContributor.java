package com.firstlinecode.granite.xeps.xdata;

import org.pf4j.Extension;

import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			XData.class
		};
	}
}
