package com.firstlinecode.granite.framework.core;

import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public interface IServer {
	void start() throws Exception;
	void stop() throws Exception;
	
	IServerConfiguration getConfiguration();
	IServerContext getServerContext();
}
