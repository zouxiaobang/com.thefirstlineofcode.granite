package com.firstlinecode.granite.framework.im;

import com.firstlinecode.basalt.protocol.core.JabberId;

public interface IResourcesService {
	IResource[] getResources(JabberId jid);
	IResource getResource(JabberId jid);
}
