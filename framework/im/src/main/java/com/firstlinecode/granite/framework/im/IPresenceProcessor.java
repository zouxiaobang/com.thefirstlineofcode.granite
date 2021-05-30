package com.firstlinecode.granite.framework.im;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.pipes.processing.IProcessingContext;

public interface IPresenceProcessor {
	boolean process(IProcessingContext context, Presence presence);
}
