package com.firstlinecode.granite.framework.processing;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IXepProcessor<S extends Stanza, X> {
	void process(IProcessingContext context, S stanza, X xep);
}
