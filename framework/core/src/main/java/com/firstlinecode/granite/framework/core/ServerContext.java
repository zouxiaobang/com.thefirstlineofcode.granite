package com.firstlinecode.granite.framework.core;

import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.integration.ApplicationComponentService;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentConfigurations;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.repository.IRepository;

public class ServerContext implements IServerContext {
	private IServerConfiguration serverConfiguration;
	private IRepository repository;
	private ApplicationComponentService appComponentService;
	
	public ServerContext(IServerConfiguration serverConfiguration, IRepository repository,
			ApplicationComponentService appComponentService) {
		this.serverConfiguration = serverConfiguration;
		this.repository = repository;
		this.appComponentService = appComponentService;
	}

	@Override
	public IServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}
	
	@Override
	public IRepository getRepository() {
		return repository;
	}
	
	@Override
	public IApplicationComponentService getApplicationComponentService() {
		return appComponentService;
	}

	@Override
	public IApplicationComponentConfigurations getApplicationComponentConfigurations() {
		return appComponentService.getApplicationComponentConfigurations();
	}
}
