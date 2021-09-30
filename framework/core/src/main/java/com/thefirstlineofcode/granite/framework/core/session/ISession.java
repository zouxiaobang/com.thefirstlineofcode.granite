package com.thefirstlineofcode.granite.framework.core.session;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;

public interface ISession {
	public static final Object KEY_SESSION_JID = "granite.session.jid";
	
	<T> T setAttribute(Object key, T value);
	<T> T setAttribute(Object key, ValueWrapper<T> wrapper);
	<T> T getAttribute(Object key);
	<T> T getAttribute(Object key, T defaultValue);
	<T> T removeAttribute(Object key);
	
	Object[] getAttributeKeys();
	
	JabberId getJid();
}
