package com.firstlinecode.granite.framework.core.integration;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManager;

public interface IClientMessageProcessor extends IMessageProcessor {
	void setConnectionManager(IConnectionManager connectionManager);
	void connectionOpened(IClientConnectionContext context);
	void connectionClosing(IClientConnectionContext context);
	void connectionClosed(IClientConnectionContext context, JabberId sessionJid);
}
