package com.firstlinecode.granite.framework.core.pipeline.stages.processing;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;

public interface IIqResultProcessor {
	boolean processResult(IProcessingContext context, Iq iq);
}
