package com.firstlinecode.granite.framework.core.internal;

import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.IServer;
import com.firstlinecode.granite.framework.core.IServerContext;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.internal.repository.Repository;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.repository.IServiceListener;
import com.firstlinecode.granite.framework.core.repository.IServiceWrapper;

public class Server implements IServer, IServiceListener {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
		
	private IServerConfiguration serverConfiguration;
	private PluginManager pluginManager;
	
	private IRepository repository;
		
	public Server(IServerConfiguration serverConfiguration, PluginManager pluginManager) {
		this.serverConfiguration = serverConfiguration;
		this.pluginManager = pluginManager;
	}

	@Override
	public void start() throws Exception {
		/*pluginManager.loadPlugins();
		pluginManager.startPlugins();*/
		
		repository = new Repository(serverConfiguration);
		repository.init();
		
		logger.info("Granite Server has Started");
	}

	@Override
	public void stop() throws Exception {
/*		pluginManager.stopPlugins();
		pluginManager.unloadPlugins();*/
		
		logger.info("Granite Server has stopped.");
	}

	@Override
	public IServerContext getContext() {
		return new ServerContext(serverConfiguration, repository, pluginManager);
	}

	@Override
	public void available(IServiceWrapper serviceWrapper) {
	}
}
