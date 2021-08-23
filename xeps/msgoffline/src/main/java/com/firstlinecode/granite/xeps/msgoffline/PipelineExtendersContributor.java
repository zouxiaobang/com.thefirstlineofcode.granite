package com.firstlinecode.granite.xeps.msgoffline;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.stages.event.EventListenerFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.im.OfflineMessageEvent;
import com.firstlinecode.granite.framework.im.ResourceAvailabledEvent;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	public IEventListenerFactory<?>[] getEventListenerFactories() {
		return new IEventListenerFactory<?>[] {
			new EventListenerFactory<>(ResourceAvailabledEvent.class, new ResourceAvailabledListener()),
			new EventListenerFactory<>(OfflineMessageEvent.class, new OfflineMessageListener())
		};
	}
}
