package com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing;

import com.thefirstlineofcode.basalt.protocol.core.stanza.Iq;
import com.thefirstlineofcode.basalt.protocol.core.stanza.error.StanzaError;

public interface IIqResultProcessor {
	boolean processResult(IProcessingContext context, Iq result);
	boolean processError(IProcessingContext context, StanzaError error);
}
