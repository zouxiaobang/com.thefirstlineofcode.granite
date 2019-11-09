package com.firstlinecode.granite.framework.core.config;

public interface IApplicationConfiguration {
	public static final String APP_CONFIG_KEY_DISABLED_SERVICES = "disabled.services";
	public static final String APP_CONFIG_KEY_DOMAIN_NAME = "domain.name";
	public static final String APP_CONFIG_KEY_DOMAIN_ALIAS_NAMES = "domain.alias.names";
	public static final String APP_CONFIG_KEY_MESSAGE_FORMAT = "message.format";
	public static final String APP_CONFIG_KEY_CONFIGURATION_MANAGER_CLASS = "configuration.manager.class";
	public static final String APP_CONFIG_KEY_CONFIGURATION_MANAGER_BUNDLE_SYMBOLIC_NAME = "configuration.manager.bundle.symbolic.name";
	public static final String APP_CONFIG_KEY_COMPONENT_BINDING_PROFILE = "component.binding.profile";
	public static final String APP_CONFIG_KEY_LOG_CONFIGURATION_FILE = "log.configuration.file";
	public static final String APP_CONFIG_KEY_APPLICATION_NAMESPACES = "application.namespaces";
	
	String getConfigurationManagerBundleSymbolicName();	
	String getConfigurationManagerClass();
	String[] getDisabledServices();
	String getAppHome();
	String getConfigDir();
	String getDomainName();
	String[] getDomainAliasNames();
	String getMessageFormat();
	String getComponentBindingProfile();
	String getLogConfigurationFile();
	String[] getApplicationNamespaces();
}
