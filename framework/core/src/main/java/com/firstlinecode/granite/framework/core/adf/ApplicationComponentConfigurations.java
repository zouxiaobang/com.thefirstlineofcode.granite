package com.firstlinecode.granite.framework.core.adf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.firstlinecode.granite.framework.core.config.DummyConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.PropertiesConfiguration;
import com.firstlinecode.granite.framework.core.utils.SectionalProperties;


public class ApplicationComponentConfigurations implements IApplicationComponentConfigurations {
	private static final String GRANITE_PACKAGE_PREFIX_PLACEHOLDER = "granite-";
	private static final String SAND_PACKAGE_PREFIX_PLACEHOLDER = "sand-";
	private static final String GRANITE_PACKAGE_PREFIX = "com.firstlinecode.granite.";
	private static final String SAND_PACKAGE_PREFIX = "com.firstlinecode.sand.";

	private static final String CONFIGURATION_FILE = "protocol-plugins.ini";
	
	private static final Map<String, String> PACKAGE_PLACEHOLDERS = new HashMap<>();
	private Map<String, PropertiesConfiguration> configurations;
	
	public ApplicationComponentConfigurations(String configDir) {
		PACKAGE_PLACEHOLDERS.put(GRANITE_PACKAGE_PREFIX_PLACEHOLDER, GRANITE_PACKAGE_PREFIX);
		PACKAGE_PLACEHOLDERS.put(SAND_PACKAGE_PREFIX_PLACEHOLDER, SAND_PACKAGE_PREFIX);
		
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
			String symbolicName;
			if (sectionName.startsWith(GRANITE_PACKAGE_PREFIX_PLACEHOLDER)) {
				symbolicName = sectionName.replace(GRANITE_PACKAGE_PREFIX_PLACEHOLDER, GRANITE_PACKAGE_PREFIX);
			} else if(sectionName.startsWith(SAND_PACKAGE_PREFIX_PLACEHOLDER)) {
				symbolicName = sectionName.replace(GRANITE_PACKAGE_PREFIX_PLACEHOLDER, GRANITE_PACKAGE_PREFIX);				
			} else {
				symbolicName = sectionName;
			}
				
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
