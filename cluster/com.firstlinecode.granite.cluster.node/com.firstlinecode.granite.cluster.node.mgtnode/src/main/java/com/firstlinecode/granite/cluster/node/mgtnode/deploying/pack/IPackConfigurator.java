package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;

public interface IPackConfigurator {
	void configure(IPackContext context, DeployPlan configuration);
}
