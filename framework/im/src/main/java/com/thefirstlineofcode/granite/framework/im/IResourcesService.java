package com.thefirstlineofcode.granite.framework.im;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;

public interface IResourcesService {
	IResource[] getResources(JabberId jid);
	IResource getResource(JabberId jid);
}
