package com.firstlinecode.granite.leps.im.traceable;

import java.util.Iterator;
import java.util.List;

import com.firstlinecode.basalt.leps.im.message.traceable.MsgStatus;
import com.firstlinecode.basalt.protocol.core.JabberId;

public interface ITraceStore {
	void save(JabberId jid, MsgStatus msgStatus);
	void save(JabberId jid, List<MsgStatus> msgStatuses);
	void remove(JabberId jid, String messageId);
	void remove(JabberId jid, List<String> messageIds);
	Iterator<MsgTrace> iterator(JabberId jid);
	boolean isEmpty(JabberId jid);
	int getSize(JabberId jid);
}
