package com.firstlinecode.granite.leps.im.traceable;

import java.util.Iterator;

import com.firstlinecode.basalt.protocol.core.JabberId;

public interface ITraceableMessageStore {
	void save(JabberId jid, String messageId, String message);
	TraceableMessage get(JabberId jid, String messageId);
	void remove(JabberId jid, String messageId);
	Iterator<TraceableMessage> iterator(JabberId jid);
	boolean isEmpty(JabberId jid);
	int getSize(JabberId jid);
}
