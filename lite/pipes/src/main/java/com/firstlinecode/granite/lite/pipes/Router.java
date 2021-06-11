package com.firstlinecode.granite.lite.pipes;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.pipes.routing.IForward;
import com.firstlinecode.granite.framework.core.pipes.routing.IRouter;
import com.firstlinecode.granite.framework.core.pipes.routing.RoutingRegistrationException;

@Component("lite.router")
public class Router implements IRouter {

	@Override
	public void register(JabberId jid, String localNodeId) throws RoutingRegistrationException {
		// do nothing
	}

	@Override
	public void unregister(JabberId jid) throws RoutingRegistrationException {
		// do nothing
	}

	@Override
	public IForward[] get(JabberId jid) {
		// this method is never called
		return null;
	}

}
