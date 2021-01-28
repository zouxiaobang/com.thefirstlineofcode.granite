package com.firstlinecode.granite.framework.core.supports;

import com.firstlinecode.granite.framework.core.config.IConfiguration;

public interface IApplicationComponentConfigurations {
	IConfiguration getConfiguration(String bundleSymbolicName);
}