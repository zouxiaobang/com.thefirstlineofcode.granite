package com.firstlinecode.granite.framework.im;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;

public interface IResource {
	JabberId getJid();
	boolean isRosterRequested();
	Presence getBroadcastPresence();
	boolean isAvailable();
	Presence getDirectedPresence(JabberId from);
}
