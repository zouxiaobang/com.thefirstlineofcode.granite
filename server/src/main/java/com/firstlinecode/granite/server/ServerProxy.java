package com.firstlinecode.granite.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.adf.mybatis.AdfMyBatisConfiguration;
import com.firstlinecode.granite.framework.adf.spring.AdfComponentService;
import com.firstlinecode.granite.framework.adf.spring.AdfServer;
import com.firstlinecode.granite.framework.core.IServer;
import com.firstlinecode.granite.framework.core.Server;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public class ServerProxy {
	private static final Logger logger = LoggerFactory.getLogger(ServerProxy.class);
	
	private IServer server;
	
	public IServer start(IServerConfiguration serverConfiguration) {
		server = createServer(serverConfiguration);
		try {
			server.start();
		} catch (Exception e) {
			if (server != null) {
				try {
					server.stop();
				} catch (Exception exception) {
					throw new RuntimeException("Can't stop server correctly.", exception);
				}
			}
			
			logger.error("Can't to start Granite Server correctly.", e);
			throw new RuntimeException("Can't to start Granite Server Correctly.", e);
		}
		
		return server;
	}

	private Server createServer(IServerConfiguration serverConfiguration) {
		return new AdfServer(serverConfiguration) {
			@Override
			protected IApplicationComponentService createAppComponentService() {
				return new AdfComponentService(configuration) {
					@Override
					protected void registerPredefinedSpringConfigurations() {
						appContext.register(AdfMyBatisConfiguration.class);;
					}
				};
			}
		};
	}
	
	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("Can't to stop Granite Server correctly.", e);
		}
	}
}
