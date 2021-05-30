package com.firstlinecode.granite.framework.im;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.pipes.processing.IProcessingContext;

public interface IIqResultProcessor {
	boolean process(IProcessingContext context, Iq iq);
}
