package com.firstlinecode.granite.pipeline.stream;

import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.pipeline.IMessage;

public interface IStreamNegotiant {
	void setNext(IStreamNegotiant next);
	boolean negotiate(IClientConnectionContext context, IMessage message);
}
