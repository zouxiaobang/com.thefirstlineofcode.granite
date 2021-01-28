package com.firstlinecode.granite.lite.leps.im.traceable;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.leps.im.traceable.MsgTrace;

public interface TraceMapper {
	void insert(MsgTrace msgTrace);
	List<MsgTrace> selectByJid(JabberId jid, int limit, int offset);
	void deleteByJidAndMessageId(JabberId jid, String messageId);
	int selectCountByJid(JabberId jid);
}
