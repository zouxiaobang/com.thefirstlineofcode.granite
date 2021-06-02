package com.firstlinecode.granite.framework.core.app;

import com.firstlinecode.granite.framework.core.config.IConfiguration;

public interface IApplicationComponentConfigurations {
	IConfiguration getConfiguration(String bundleSymbolicName);
}