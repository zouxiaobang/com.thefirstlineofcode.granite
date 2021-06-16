package com.firstlinecode.granite.framework.core.pipeline;

import com.firstlinecode.granite.framework.core.connection.IConnectionContext;

public interface IMessageProcessor {
	void process(IConnectionContext context, IMessage message);
}
