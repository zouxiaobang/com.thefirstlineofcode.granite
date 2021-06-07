package com.firstlinecode.granite.framework.core.pipes.processing;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.granite.framework.core.pipes.IPipeExtender;

public interface IXepProcessor<S extends Stanza, X> extends IPipeExtender {
	void process(IProcessingContext context, S stanza, X xep);
}
