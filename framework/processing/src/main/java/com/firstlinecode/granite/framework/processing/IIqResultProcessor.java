package com.firstlinecode.granite.framework.processing;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;

public interface IIqResultProcessor {
	boolean process(IProcessingContext context, Iq iq);
}
