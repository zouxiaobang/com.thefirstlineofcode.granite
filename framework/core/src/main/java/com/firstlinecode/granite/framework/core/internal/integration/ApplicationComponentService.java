package com.firstlinecode.granite.framework.core.internal.integration;

import java.util.List;

import org.pf4j.PluginManager;

import com.firstlinecode.granite.framework.core.integration.IApplicationComponentConfigurations;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentService;

public class ApplicationComponentService implements IApplicationComponentService {
	private PluginManager pluginManager;
	private IApplicationComponentConfigurations appComponentConfigurations;
	private boolean started;

	public ApplicationComponentService(PluginManager pluginManager,
			IApplicationComponentConfigurations appComponentConfigurations) {
		this.pluginManager = pluginManager;
		this.appComponentConfigurations = appComponentConfigurations;
	}
	
	public PluginManager getPluginManager() {
		return pluginManager;
	}
	
	public IApplicationComponentConfigurations getApplicationComponentConfigurations() {
		return appComponentConfigurations;
	}

	@Override
	public <T> List<Class<? extends T>> getExtensionClasses(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T createExtension(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() {
		if (started)
			return;
		
		pluginManager.loadPlugins();
		pluginManager.startPlugins();
		
		started = true;
	}

	@Override
	public void stop() {
		if (!started)
			return;
		
		pluginManager.stopPlugins();
		pluginManager.unloadPlugins();
		
		started = false;
	}

}
