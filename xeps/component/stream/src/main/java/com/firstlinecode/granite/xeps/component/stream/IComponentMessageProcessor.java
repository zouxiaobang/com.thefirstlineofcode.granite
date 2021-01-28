package com.firstlinecode.granite.xeps.component.stream;

import com.firstlinecode.granite.framework.core.integration.IMessageProcessor;

public interface IComponentMessageProcessor extends IMessageProcessor {
	void setComponentConnectionsRegister(IComponentConnectionsRegister connectionsRegister);
}
