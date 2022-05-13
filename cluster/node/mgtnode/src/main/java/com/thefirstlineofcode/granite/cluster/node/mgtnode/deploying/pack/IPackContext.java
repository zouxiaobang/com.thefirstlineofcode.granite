package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack;

import java.nio.file.Path;

import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlan;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.config.IConfigManager;

public interface IPackContext {
	Path getConfigurationDir();
	Path getClusterConfigurationDir();
	Path getRepositoryDir();
	Path getRuntimeDir();
	Path getRuntimeLibsDir();
	Path getRuntimePluginsDir();
	String getNodeType();
	DeployPlan getDeployPlan();
	IPackModule getPackModule(String moduleName);
	IConfigManager getConfigManager();
}
