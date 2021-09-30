package com.thefirstlineofcode.granite.framework.core.connection;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;

public interface IConnectionManager {
	IConnectionContext getConnectionContext(JabberId sessionJid);
}
