package com.firstlinecode.granite.xeps.disco;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessor;

public class DiscoItemsProcessor implements IXepProcessor<Iq, DiscoItems> {
	@Dependency("disco.processor")
	private IDiscoProcessor discoProcessor;

	@Override
	public void process(IProcessingContext context, Iq iq, DiscoItems discoItems) {
		discoProcessor.discoItems(context, iq, iq.getTo() == null ? context.getJid() : iq.getTo(), discoItems.getNode());
	}

}
