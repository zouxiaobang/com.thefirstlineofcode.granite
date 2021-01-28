package com.firstlinecode.granite.framework.processing;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;

public interface IPresenceProcessor {
	boolean process(IProcessingContext context, Presence presence);
}
