package com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing;

import com.thefirstlineofcode.basalt.protocol.core.stanza.Iq;
import com.thefirstlineofcode.basalt.protocol.core.stanza.error.StanzaError;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtender;

public interface IIqResultProcessor extends IPipelineExtender {
	boolean processResult(IProcessingContext context, Iq result);
	boolean processError(IProcessingContext context, StanzaError error);
}
