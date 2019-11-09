package com.firstlinecode.granite.lite.integration;

import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.routing.ILocalNodeIdProvider;

@Component("lite.local.node.id.provider")
public class LocalNodeIdProvider implements ILocalNodeIdProvider {

	@Override
	public String getLocalNodeId() {
		return null;
	}

}
