package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager implements IConfigManager {
	private Map<ConfigPath, IConfig> configs;
	
	public ConfigManager() {
		configs = new HashMap<>();
	}
	
	@Override
	public IConfig createOrGetConfig(Path parentPath, String configFileName) {
		if (parentPath == null)
			throw new IllegalArgumentException("null parent path");
		
		if (configFileName == null)
			throw new IllegalArgumentException("null config file name");
		
		ConfigPath configPath = new ConfigPath(parentPath, configFileName);
		IConfig config = configs.get(configPath);
		if (config == null) {
			config = new Config();
			configs.put(configPath, config);
		}
		
		return config;
	}

	@Override
	public void saveConfigs() {
		for (ConfigPath configPath : configs.keySet()) {
			configs.get(configPath).save(configPath.parentPath, configPath.configFileName);
		}
	}
	
	private class ConfigPath {
		public Path parentPath;
		public String configFileName;
		
		public ConfigPath(Path parentPath, String configFileName) {
			this.parentPath = parentPath;
			this.configFileName = configFileName;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ConfigPath) {
				ConfigPath other = (ConfigPath)obj;
				
				return other.parentPath.equals(parentPath) && other.configFileName.equals(configFileName);
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return 7 + 31 * parentPath.hashCode() + configFileName.hashCode();
		}
	}

}
