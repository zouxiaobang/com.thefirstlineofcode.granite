package com.firstlinecode.granite.framework.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public class ServerProxy {
	private static final Logger logger = LoggerFactory.getLogger(ServerProxy.class);
	
	private IServer server;
	
	public IServer start(IServerConfiguration serverConfiguration, IApplicationComponentService appComponentService) {
		server = createServer(serverConfiguration, appComponentService);
		try {
			server.start();
		} catch (Exception e) {
			logger.error("Can't to start Granite Server correctly.", e);
			throw new RuntimeException("Can't to start Granite Server Correctly.", e);
		}
		
		return server;
	}

	protected Server createServer(IServerConfiguration serverConfiguration, IApplicationComponentService appComponentService) {
		return new Server(serverConfiguration, appComponentService);
	}
	
	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("Can't to stop Granite Server correctly.", e);
		}
	}
}
