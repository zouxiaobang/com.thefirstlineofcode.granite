package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack;

import java.nio.file.Path;
import java.util.Map;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.ConfigManager;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.IConfigManager;

public class PackContext implements IPackContext {
	private Path configDir;
	private Path repositoryDirPath;
	private Path runtimeDirPath;
	private Path pluginsDirPath;
	private Path osgiConfigDirPath;
	private Path graniteConfigPath;
	private Map<String, IPackModule> packModules;
	private String nodeType;
	private DeployPlan configuration;
	private IConfigManager configManager;
	
	public PackContext(Path configDir, Path repositoryDirPath, Path runtimeDirPath, Path pluginsDirPath,
			Path osgiConfigDirPath, Path graniteConfigPath, Map<String, IPackModule> packModules,
				String nodeType, DeployPlan configuration) {
		this.configDir = configDir;
		this.repositoryDirPath = repositoryDirPath;
		this.runtimeDirPath = runtimeDirPath;
		this.pluginsDirPath = pluginsDirPath;
		this.osgiConfigDirPath = osgiConfigDirPath;
		this.graniteConfigPath = graniteConfigPath;
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
	public Path getRuntimeOsgiConfigDir() {
		return osgiConfigDirPath;
	}

	@Override
	public Path getRuntimeGraniteConfigDir() {
		return graniteConfigPath;
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
	public Path getConfigDir() {
		return configDir;
	}
	
}
