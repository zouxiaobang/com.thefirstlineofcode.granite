package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager implements IConfigManager {
	private Map<Path, IConfig> configs;
	
	public ConfigManager() {
		configs = new HashMap<>();
	}
	
	@Override
	public IConfig createOrGetConfig(Path parentPath, String configFileName) {
		if (parentPath == null)
			throw new IllegalArgumentException("Null parent path.");
		
		if (configFileName == null)
			throw new IllegalArgumentException("Null config file name.");
		
		Path configPath = parentPath.resolve(configFileName);
		IConfig config = configs.get(configPath);
		if (config == null) {
			config = new Config();
			configs.put(configPath, config);
		}
		
		return config;
	}

	@Override
	public void saveConfigs() {
		for (Path configPath : configs.keySet()) {
			configs.get(configPath).save(configPath);
		}
	}

}
