package com.thefirstlineofcode.granite.framework.im;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.im.stanza.Presence;

public interface IResource {
	JabberId getJid();
	boolean isRosterRequested();
	Presence getBroadcastPresence();
	boolean isAvailable();
	Presence getDirectedPresence(JabberId from);
}
