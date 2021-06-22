package com.firstlinecode.granite.framework.core.adf;

import com.firstlinecode.granite.framework.core.config.IConfiguration;

public interface IApplicationComponentConfigurations {
	IConfiguration getConfiguration(String pluginId);
}