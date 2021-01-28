package com.firstlinecode.granite.xeps.component.stream.internal;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManager;
import com.firstlinecode.granite.framework.stream.IDeliveryMessageReceiver;
import com.firstlinecode.granite.xeps.component.stream.IComponentConnectionsRegister;
import com.firstlinecode.granite.xeps.component.stream.IComponentMessageAcceptor;

@Component("component.stream.service")
public class ComponentStreamService implements IService {
	
	@Dependency("message.acceptors")
	private List<IComponentMessageAcceptor> componentMessageAcceptors;
	
	@Dependency("delivery.message.receiver")
	private IDeliveryMessageReceiver deliveryMessageReceiver;
	
	@Dependency("component.connections.register")
	private IComponentConnectionsRegister connectionsRegister;
	
	@Override
	public void start() throws Exception {
		if (deliveryMessageReceiver != null) {
			IConnectionManager connectionManager = getConnectionManager();
			
			if (connectionManager != null)
				deliveryMessageReceiver.setConnectionManager(connectionManager);
			
			deliveryMessageReceiver.start();
		}
		
		if (componentMessageAcceptors != null) {
			for (IComponentMessageAcceptor acceptor : componentMessageAcceptors) {
				acceptor.setComponentConnectionsRegister(connectionsRegister);
				acceptor.start();
			}
		}
	}

	private IConnectionManager getConnectionManager() {
		return new IConnectionManager() {
			@Override
			public IConnectionContext getConnectionContext(JabberId sessionJid) {
				for (IComponentMessageAcceptor acceptor : componentMessageAcceptors) {
					IConnectionContext context = acceptor.getConnectionContext(sessionJid);
					
					if (context != null)
						return context;
				}
				
				return null;
			}
		};
	}

	@Override
	public void stop() throws Exception {
		if (componentMessageAcceptors != null) {
			for (IComponentMessageAcceptor acceptor : componentMessageAcceptors) {
				acceptor.stop();
			}
		}
		
		if (deliveryMessageReceiver != null)
			deliveryMessageReceiver.stop();
	}
	
}
