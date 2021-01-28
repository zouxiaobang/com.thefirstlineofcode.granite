package com.firstlinecode.granite.framework.stream;

import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.integration.IMessage;

public interface IStreamNegotiant {
	void setNext(IStreamNegotiant next);
	boolean negotiate(IClientConnectionContext context, IMessage message);
}
