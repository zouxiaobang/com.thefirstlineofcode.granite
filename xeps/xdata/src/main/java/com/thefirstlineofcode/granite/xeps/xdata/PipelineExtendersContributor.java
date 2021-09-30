package com.thefirstlineofcode.granite.xeps.xdata;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xeps.xdata.XData;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			XData.class
		};
	}
}
