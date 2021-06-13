package com.firstlinecode.granite.framework.core.pipes.processing;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.granite.framework.core.pipes.IPipesExtender;

public interface IXepProcessor<S extends Stanza, X> extends IPipesExtender {
	void process(IProcessingContext context, S stanza, X xep);
}
