package com.firstlinecode.granite.framework.im;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.pipeline.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.utils.OrderComparator;

public class DefaultPresenceProcessor implements IPresenceProcessor {
	private List<IPresenceProcessor> presenceProcessors;
	
	public DefaultPresenceProcessor() {
		presenceProcessors = new ArrayList<>();
	}
	
	public void setPresenceProcessors(List<IPresenceProcessor> presenceProcessors) {
		this.presenceProcessors = presenceProcessors;
		Collections.sort(presenceProcessors, new OrderComparator<>());
	}
	
	public void addPresenceProcessor(IPresenceProcessor presenceProcessor) {
		if (!presenceProcessors.contains(presenceProcessor)) {
			presenceProcessors.add(presenceProcessor);
			Collections.sort(presenceProcessors, new OrderComparator<>());
		}
	}
	
	@Override
	public boolean process(IProcessingContext context, Presence presence) {
		for (IPresenceProcessor presenceProcessor : presenceProcessors) {
			if (presenceProcessor.process(context, presence))
				return true;
		}
		
		return false;
	}

}
