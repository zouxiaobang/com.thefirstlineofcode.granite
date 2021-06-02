package com.firstlinecode.granite.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.app.ApplicationComponentService;
import com.firstlinecode.granite.framework.core.app.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.repository.IServiceListener;
import com.firstlinecode.granite.framework.core.repository.IServiceWrapper;
import com.firstlinecode.granite.framework.core.repository.Repository;

public class Server implements IServer, IServiceListener {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
		
	private IServerConfiguration serverConfiguration;
	
	private IApplicationComponentService appComponentService;
	private IRepository repository;
		
	public Server(IServerConfiguration serverConfiguration, IApplicationComponentService appComponentService) {
		this.serverConfiguration = serverConfiguration;
		this.appComponentService = appComponentService;
	}

	@Override
	public void start() throws Exception {
		appComponentService.start();
		
		repository = new Repository(serverConfiguration, appComponentService);
		repository.init();
		
		logger.info("Granite Server has Started");
	}

	@Override
	public void stop() throws Exception {
		appComponentService.stop();
		
		logger.info("Granite Server has stopped.");
	}

	@Override
	public IServerContext getServerContext() {
		return new ServerContext(serverConfiguration, repository, (ApplicationComponentService)appComponentService);
	}

	@Override
	public void available(IServiceWrapper serviceWrapper) {
	}
}
