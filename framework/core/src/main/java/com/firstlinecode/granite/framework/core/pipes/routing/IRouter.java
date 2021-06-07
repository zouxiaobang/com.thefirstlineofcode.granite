package com.firstlinecode.granite.framework.core.pipes.routing;

import com.firstlinecode.basalt.protocol.core.JabberId;

public interface IRouter {
	void register(JabberId jid, String localNodeId) throws RoutingRegistrationException;
	void unregister(JabberId jid) throws RoutingRegistrationException;
	
	IForward[] get(JabberId jid);
}
