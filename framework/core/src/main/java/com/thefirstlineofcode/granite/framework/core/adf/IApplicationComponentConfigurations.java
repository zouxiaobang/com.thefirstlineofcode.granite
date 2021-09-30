package com.thefirstlineofcode.granite.framework.core.adf;

import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;

public interface IApplicationComponentConfigurations {
	IConfiguration getConfiguration(String pluginId);
}