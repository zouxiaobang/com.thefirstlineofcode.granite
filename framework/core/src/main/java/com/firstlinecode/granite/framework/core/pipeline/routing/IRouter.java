package com.firstlinecode.granite.framework.core.pipeline.routing;

import com.firstlinecode.basalt.protocol.core.JabberId;

public interface IRouter {
	void register(JabberId jid, String localNodeId) throws RoutingRegistrationException;
	void unregister(JabberId jid) throws RoutingRegistrationException;
	
	IForward[] get(JabberId jid);
}
