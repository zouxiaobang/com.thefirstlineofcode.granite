package com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing;

import com.thefirstlineofcode.basalt.protocol.core.stanza.Stanza;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtender;

public interface IXepProcessor<S extends Stanza, X> extends IPipelineExtender {
	void process(IProcessingContext context, S stanza, X xep);
}
