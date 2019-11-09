package com.firstlinecode.granite.xeps.muc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.session.ISession;

public abstract class MucSessionUtils {
	private MucSessionUtils() {}
	
	public static final String SESSION_KEY_MUC_ROOMJID_AND_NICK_MAP = "granite.session.key.muc.roomjid.and.nick.map";
	
	public static Map<JabberId, String> getOrCreateRoomJidAndNickMap(ISession session) {
		Map<JabberId, String> roomJidAndNickMap = session.getAttribute(SESSION_KEY_MUC_ROOMJID_AND_NICK_MAP);
		if (roomJidAndNickMap == null) {
			roomJidAndNickMap = new ConcurrentHashMap<>();
			Map<JabberId, String> previous = session.setAttribute(SESSION_KEY_MUC_ROOMJID_AND_NICK_MAP,
					roomJidAndNickMap);
			if (previous != null) {
				roomJidAndNickMap = previous;
			}
		}
		
		return roomJidAndNickMap;
	}
}
