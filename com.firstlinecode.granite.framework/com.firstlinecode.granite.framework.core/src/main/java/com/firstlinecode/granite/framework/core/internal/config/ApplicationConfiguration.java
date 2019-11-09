package com.firstlinecode.granite.framework.core.internal.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.granite.framework.core.commons.utils.IoUtils;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;


public class ApplicationConfiguration implements IApplicationConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);
	
	private static final String NAME_APPLICATION_CONFIG_FILE = "application.ini";
	
	private String domainName = "localhost";
	private String[] domainAliasNames = new String[0];
	private String messageFormat = Constants.MESSAGE_FORMAT_XML;

	private String[] disabledServices = new String[0];
	private String componentBindingProfile = "${config.dir}/component-binding.ini";
	private String logConfigurationFile;
	
	private String appHome;
	private String configDir;
	private String configurationManagerBundleSymbolicName;
	private String configurationManagerClass;
	private String[] applicationNamespaces;
	
	public ApplicationConfiguration(String appHome, String configDir) {
		this.appHome = appHome;
		this.configDir = configDir;
		
		merge(getAppConfigFile(configDir));
	}
	
	private File getAppConfigFile(String configDir) {
		File appConfigFile = new File(configDir, NAME_APPLICATION_CONFIG_FILE);
		if (appConfigFile.exists() && appConfigFile.isFile())
			return appConfigFile;
		
		return null;
	}

	@Override
	public String getConfigurationManagerBundleSymbolicName() {
		return configurationManagerBundleSymbolicName;
	}

	@Override
	public String getConfigurationManagerClass() {
		return configurationManagerClass;
	}

	@Override
	public String[] getDisabledServices() {
		return disabledServices;
	}

	@Override
	public String getAppHome() {
		return appHome;
	}

	private void merge(File configFile) {
		if (configFile == null)
			return;

		Properties properties = new Properties();
		Reader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(configFile));
			properties.load(reader);
			
			for (Object key : properties.keySet()) {
				if (APP_CONFIG_KEY_CONFIGURATION_MANAGER_BUNDLE_SYMBOLIC_NAME.equals(key)) {
					configurationManagerBundleSymbolicName = properties.getProperty((String)key);
				} else if (APP_CONFIG_KEY_CONFIGURATION_MANAGER_CLASS.equals(key)) {
					configurationManagerClass = properties.getProperty((String)key);
				} else if (APP_CONFIG_KEY_DOMAIN_NAME.equals(key)) {
					domainName = properties.getProperty((String)key);
				} else if (APP_CONFIG_KEY_DOMAIN_ALIAS_NAMES.equals(key)) {
					setDomainAliasNames(properties.getProperty((String)key));
				} else if (APP_CONFIG_KEY_MESSAGE_FORMAT.equals(key)) {
					setMessageFormat(properties.getProperty((String)key));
				} else if (APP_CONFIG_KEY_DISABLED_SERVICES.equals(key)) {
					setDisabledServices(properties.getProperty((String)key));
				} else if (APP_CONFIG_KEY_APPLICATION_NAMESPACES.equals(key)) {
					setApplicationNamespaces(properties.getProperty((String)key));
				} else if (APP_CONFIG_KEY_COMPONENT_BINDING_PROFILE.equals(key)) {
					componentBindingProfile = properties.getProperty((String)key);
				} else if (APP_CONFIG_KEY_LOG_CONFIGURATION_FILE.equals(key)) {
					logConfigurationFile = properties.getProperty((String)key);
				} else {
					// ignore
					logger.warn("Unknown application configuration item: '{}'.", key);
				}
			}
		} catch (Exception e) {
			// do nothing
		} finally {
			IoUtils.closeIO(reader);
		}
	}
	
	private void setMessageFormat(String messageFormat) {
		if (Constants.MESSAGE_FORMAT_BINARY.equals(messageFormat) || Constants.MESSAGE_FORMAT_XML.equals(messageFormat)) {
			this.messageFormat = messageFormat;
		} else {
			// ignore
			logger.warn("Unknown message format: '{}'. Continue to use 'xml' message format.", messageFormat);
		}
	}
	
	@Override
	public String getMessageFormat() {
		return messageFormat;
	}

	private void setApplicationNamespaces(String sApplicationNamespaces) {
		if (sApplicationNamespaces == null || "".equals(sApplicationNamespaces))
			return;
		
		StringTokenizer tokenizer = new StringTokenizer(sApplicationNamespaces, ",");
		applicationNamespaces = new String[tokenizer.countTokens()];
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			applicationNamespaces[i] = tokenizer.nextToken().trim();
			i++;
		}
	}

	private String replacePathVariables(String value) {
		value = value.replace("${config.dir}", configDir);
		value = value.replace("${user.home}", System.getProperty("user.home"));
		
		return value;
	}

	private void setDisabledServices(String sDisabledServices) {
		StringTokenizer tokenizer = new StringTokenizer(sDisabledServices, ",");
		disabledServices = new String[tokenizer.countTokens()];
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			disabledServices[i] = tokenizer.nextToken().trim();
			i++;
		}
	}
	
	private void setDomainAliasNames(String sDomainAliasNames) {
		StringTokenizer tokenizer = new StringTokenizer(sDomainAliasNames, ",");
		domainAliasNames = new String[tokenizer.countTokens()];
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			domainAliasNames[i] = tokenizer.nextToken().trim();
			i++;
		}
	}
	
	@Override
	public String getDomainName() {
		return domainName;
	}

	@Override
	public String getComponentBindingProfile() {
		return replacePathVariables(componentBindingProfile);
	}
	
	public void setComponentBindingProfile(String componentBindingProfile) {
		this.componentBindingProfile = componentBindingProfile;
	}
	
	@Override
	public String getConfigDir() {
		return configDir;
	}

	@Override
	public String getLogConfigurationFile() {
		if (logConfigurationFile == null)
			return null;
		
		return replacePathVariables(logConfigurationFile);
	}

	@Override
	public String[] getApplicationNamespaces() {
		return applicationNamespaces;
	}

	@Override
	public String[] getDomainAliasNames() {
		if (domainAliasNames == null) {
			domainAliasNames = new String[0];
		}
		
		return domainAliasNames;
	}
	
}
