package com.firstlinecode.granite.framework.adf.spring;

import org.pf4j.PluginManager;

import com.firstlinecode.granite.framework.core.adf.ApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public class AdfComponentService extends ApplicationComponentService {

	public AdfComponentService(IServerConfiguration serverConfiguration) {
		super(serverConfiguration);
	}
	
	public AdfComponentService(IServerConfiguration serverConfiguration, AdfPluginManager pluginManager) {
		super(serverConfiguration, pluginManager);
	}
		
	public AdfComponentService(IServerConfiguration serverConfiguration, AdfPluginManager pluginManager,
			boolean syncPlugins) {
		super(serverConfiguration, pluginManager, syncPlugins);
	}
	
	@Override
	protected PluginManager createPluginManager() {
		AdfPluginManager pluginManager = new AdfPluginManager();
		pluginManager.setApplicationComponentService(this);
		
		return pluginManager;
	}

}
