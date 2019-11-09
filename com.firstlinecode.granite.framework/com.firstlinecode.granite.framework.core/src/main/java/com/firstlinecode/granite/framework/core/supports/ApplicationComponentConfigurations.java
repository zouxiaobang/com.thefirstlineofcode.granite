package com.firstlinecode.granite.framework.core.supports;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.firstlinecode.granite.framework.core.commons.utils.SectionalProperties;
import com.firstlinecode.granite.framework.core.config.DummyConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.PropertiesConfiguration;


public class ApplicationComponentConfigurations implements IApplicationComponentConfigurations {
	private static final String PACKAGE_PREFIX_GRANITE_PROJECT = "com.firstlinecode.granite.";

	private static final String CONFIGURATION_FILE = "protocol-plugins.ini";
	
	private Map<String, PropertiesConfiguration> configurations;
	
	public ApplicationComponentConfigurations(String configDir) {
		File configFile = new File(configDir + "/" + CONFIGURATION_FILE);
		SectionalProperties sp = new SectionalProperties();
		if (configFile.exists()) {
			InputStream inputStream = null;
			try {
				inputStream = new BufferedInputStream(new FileInputStream(configFile));
				sp.load(inputStream);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		readConfigurations(sp);
	}
	
	private void readConfigurations(SectionalProperties sp) {
		configurations = new HashMap<>();
		for (String sectionName : sp.getSectionNames()) {
			String symbolicName = PACKAGE_PREFIX_GRANITE_PROJECT + sectionName;
			PropertiesConfiguration configuration = new PropertiesConfiguration(sp.getSection(sectionName));
			configurations.put(symbolicName, configuration);
		}
	}
	
	@Override
	public IConfiguration getConfiguration(String symbolicName) {
		IConfiguration configuration = configurations.get(symbolicName);
		
		if (configuration == null) {
			configuration = new DummyConfiguration();
		}
		
		return configuration;
	}
}
