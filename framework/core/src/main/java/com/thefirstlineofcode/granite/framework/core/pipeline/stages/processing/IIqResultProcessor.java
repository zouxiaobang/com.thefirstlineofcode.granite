package com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing;

import com.thefirstlineofcode.basalt.protocol.core.stanza.Iq;

public interface IIqResultProcessor {
	boolean processResult(IProcessingContext context, Iq iq);
}
