package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;

public interface IAppnodeRuntimesPacker {
	void pack(String nodeTypeName, String runtimeName, DeployPlan configuration);
}
