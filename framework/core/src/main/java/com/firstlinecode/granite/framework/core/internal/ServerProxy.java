package com.firstlinecode.granite.framework.core.internal;


import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.IServer;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public class ServerProxy {
	private static final Logger logger = LoggerFactory.getLogger(ServerProxy.class);
	
	private IServer server;
	
	public IServer start(IServerConfiguration serverConfiguration, PluginManager pluginManager) {
		server = new Server(serverConfiguration, pluginManager);
		try {
			server.start();
		} catch (Exception e) {
			logger.error("Can't to start Granite Server correctly.", e);
		}
		
		return server;
	}
	
	public void stop(PluginManager pluginManager) {
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("Can't to stop Granite Server correctly.", e);
		}
	}
}
