package com.thefirstlineofcode.granite.framework.core.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.granite.framework.core.utils.IoUtils;
import com.thefirstlineofcode.granite.framework.core.utils.SectionalProperties;


public class LocalFileConfigurationManager implements IConfigurationManager {
	private static final String CONFIGURATION_FILE = "components.ini";
	private static final Logger logger = LoggerFactory.getLogger(LocalFileConfigurationManager.class);
	
	private Map<String, PropertiesConfiguration> configurations;
	
	public LocalFileConfigurationManager(String configDir) {
		File configFile = new File(configDir + "/" + CONFIGURATION_FILE);
		SectionalProperties sectionalProperties = new SectionalProperties();
		if (configFile.exists()) {
			InputStream input = null;
			try {
				input = new BufferedInputStream(new FileInputStream(configFile));
				sectionalProperties.load(input);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				IoUtils.closeIO(input);
			}
		}
		
		configurations = new HashMap<>();
		for (String sectionName : sectionalProperties.getSectionNames()) {
			configurations.put(sectionName, new PropertiesConfiguration(sectionalProperties.getSection(sectionName)));
		}
	}

	@Override
	public IConfiguration getConfiguration(String componentId) {
		IConfiguration config = configurations.get(componentId);
		if (config == null) {
			logger.debug("Configuration of component[{}] not found. Instead of using a dummy configuration.",
					componentId);
			
			config = new DummyConfiguration();
		}
		
		return config;
	}
	
}
