package com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;

public interface IRouter {
	void register(JabberId jid, String localNodeId) throws RoutingRegistrationException;
	void unregister(JabberId jid) throws RoutingRegistrationException;
	
	IForward[] get(JabberId jid);
}
