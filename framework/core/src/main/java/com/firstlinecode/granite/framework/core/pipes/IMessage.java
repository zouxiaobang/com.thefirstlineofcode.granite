package com.firstlinecode.granite.framework.core.pipes;

import java.util.Map;

import com.firstlinecode.granite.framework.core.session.ISession;

public interface IMessage {
	public static final String KEY_SESSION_JID = (String)ISession.KEY_SESSION_JID;
	public static final String KEY_MESSAGE_TARGET = "granite.message.target";
	
	Map<Object, Object> getHeaders();
	Object getPayload();
}
