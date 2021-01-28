package com.firstlinecode.granite.xeps.component.stream;

import com.firstlinecode.granite.framework.core.integration.IMessageReceiver;

public interface IComponentMessageAcceptor extends IMessageReceiver {
	void setComponentConnectionsRegister(IComponentConnectionsRegister connectionsRegister);
}
