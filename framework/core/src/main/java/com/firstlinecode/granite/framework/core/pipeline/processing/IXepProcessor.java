package com.firstlinecode.granite.framework.core.pipeline.processing;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.granite.framework.core.pipeline.IPipelineExtender;

public interface IXepProcessor<S extends Stanza, X> extends IPipelineExtender {
	void process(IProcessingContext context, S stanza, X xep);
}
