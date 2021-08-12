package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.xeps.muc.Role;

public interface IRoomRuntimeInstance {
	void setSubject(Message subject);
	Message getSubject();
	Occupant[] getOccupants();
	Occupant getOccupant(String nick);
	void enter(JabberId sessionJid, String nick, Role role);
	void exit(JabberId sessionJid);
	void addToDiscussionHistory(Message message);
	Message[] getDiscussionHistory();
	void changeNick(JabberId sessionJid, String nick);
}
