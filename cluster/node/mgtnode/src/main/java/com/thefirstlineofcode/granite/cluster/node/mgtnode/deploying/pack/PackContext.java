package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack;

import java.nio.file.Path;
import java.util.Map;

import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlan;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.config.ConfigManager;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.config.IConfigManager;

public class PackContext implements IPackContext {
	private Path configurationDir;
	private Path clusterConfigurationDir;
	private Path repositoryDirPath;
	private Path runtimeDirPath;
	private Path libsDirPath;
	private Path pluginsDirPath;
	private Map<String, IPackModule> packModules;
	private String nodeType;
	private DeployPlan configuration;
	private IConfigManager configManager;
	
	public PackContext(Path configurationDir, Path clusterConfigurationDir, Path repositoryDirPath,
			Path runtimeDirPath, Path libsDirPath, Path pluginsDirPath,
			Map<String, IPackModule> packModules, String nodeType,
			DeployPlan configuration) {
		this.configurationDir = configurationDir;
		this.clusterConfigurationDir = clusterConfigurationDir;
		this.repositoryDirPath = repositoryDirPath;
		this.runtimeDirPath = runtimeDirPath;
		this.libsDirPath = libsDirPath;
		this.pluginsDirPath = pluginsDirPath;
		this.packModules = packModules;
		this.nodeType = nodeType;
		this.configuration = configuration;
		
		configManager = new ConfigManager();
	}

	@Override
	public IPackModule getPackModule(String moduleName) {
		return packModules.get(moduleName);
	}

	@Override
	public Path getRepositoryDir() {
		return repositoryDirPath;
	}

	@Override
	public Path getRuntimePluginsDir() {
		return pluginsDirPath;
	}

	@Override
	public IConfigManager getConfigManager() {
		return configManager;
	}
	
	@Override
	public String getNodeType() {
		return nodeType;
	}
	
	@Override
	public DeployPlan getDeployConfiguration() {
		return configuration;
	}

	@Override
	public Path getRuntimeDir() {
		return runtimeDirPath;
	}

	@Override
	public Path getConfigurationDir() {
		return configurationDir;
	}

	@Override
	public Path getClusterConfigurationDir() {
		return clusterConfigurationDir;
	}

	@Override
	public Path getRuntimeLibsDir() {
		return libsDirPath;
	}
	
}
