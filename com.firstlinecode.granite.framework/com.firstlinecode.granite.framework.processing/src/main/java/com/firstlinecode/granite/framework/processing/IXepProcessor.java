package com.firstlinecode.granite.framework.processing;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IXepProcessor<K extends Stanza, V> {
	void process(IProcessingContext context, K stanza, V xep);
}
