package com.firstlinecode.granite.framework.core.connection;

import com.firstlinecode.basalt.protocol.core.JabberId;

public interface IConnectionManager {
	IConnectionContext getConnectionContext(JabberId sessionJid);
}
