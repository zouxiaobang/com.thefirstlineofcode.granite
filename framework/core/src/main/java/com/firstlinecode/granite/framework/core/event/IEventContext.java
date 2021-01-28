package com.firstlinecode.granite.framework.core.event;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IEventContext {
	void write(Stanza stanza);
	void write(JabberId target, Stanza stanza);
	void write(JabberId target, String message);
}
