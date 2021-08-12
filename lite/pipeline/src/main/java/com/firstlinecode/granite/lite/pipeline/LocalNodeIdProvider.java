package com.firstlinecode.granite.lite.pipeline;

import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.ILocalNodeIdProvider;

@Component("lite.local.node.id.provider")
public class LocalNodeIdProvider implements ILocalNodeIdProvider {

	@Override
	public String getLocalNodeId() {
		return null;
	}

}
