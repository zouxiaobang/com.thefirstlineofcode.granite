package com.thefirstlineofcode.granite.xeps.muc;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.im.stanza.Message;
import com.thefirstlineofcode.basalt.xeps.muc.Role;

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
