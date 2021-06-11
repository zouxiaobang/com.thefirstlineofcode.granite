package com.firstlinecode.granite.lite.pipes;

import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.pipes.routing.ILocalNodeIdProvider;

@Component("lite.local.node.id.provider")
public class LocalNodeIdProvider implements ILocalNodeIdProvider {

	@Override
	public String getLocalNodeId() {
		return null;
	}

}
