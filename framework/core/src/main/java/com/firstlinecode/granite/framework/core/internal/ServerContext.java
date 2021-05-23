package com.firstlinecode.granite.framework.core.internal;

import org.pf4j.PluginManager;

import com.firstlinecode.granite.framework.core.IServerContext;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.repository.IRepository;

public class ServerContext implements IServerContext {
	private IServerConfiguration serverConfiguration;
	private IRepository repository;
	private PluginManager pluginManager;
	
	public ServerContext(IServerConfiguration serverConfiguration, IRepository repository, PluginManager pluginManager) {
		this.serverConfiguration = serverConfiguration;
		this.repository = repository;
		this.pluginManager = pluginManager;
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
	public PluginManager getPluginManager() {
		return pluginManager;
	}
}
