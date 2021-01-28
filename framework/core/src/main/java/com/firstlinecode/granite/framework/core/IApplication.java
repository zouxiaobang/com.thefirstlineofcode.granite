package com.firstlinecode.granite.framework.core;

import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;

public interface IApplication {
	public static final String GRANITE_APP_COMPONENT_COLLECTOR = "granite.app.component.collector";
	
	void start() throws Exception;
	void stop() throws Exception;
	
	IApplicationConfiguration getConfiguration();
}
