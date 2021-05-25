package com.firstlinecode.granite.framework.core.internal.integration;

import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentConfigurations;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.integration.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

public class ApplicationComponentService implements IApplicationComponentService {
	private IServerConfiguration serverConfiguration;
	private PluginManager pluginManager;
	private IApplicationComponentConfigurations appComponentConfigurations;
	private boolean started;

	public ApplicationComponentService(IServerConfiguration serverConfiguration, PluginManager pluginManager,
			IApplicationComponentConfigurations appComponentConfigurations) {
		this.serverConfiguration = serverConfiguration;
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
		return pluginManager.getExtensionClasses(type);
	}

	@Override
	public <T> T createExtension(Class<T> type) {
		PluginWrapper plugin = pluginManager.whichPlugin(type);
		if (plugin == null)
			throw new IllegalArgumentException("Can't determine the class %s is loaded from which plugin.");
		
		T extension = null;
		try {
			extension = type.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't create extension which's type is %s", type.getName()), e);
		}
		
		if (extension instanceof IServerConfigurationAware) {
			((IServerConfigurationAware)extension).setServerConfiguration(serverConfiguration);
		}
		
		if (extension instanceof IConfigurationAware) {
			IConfiguration configuration = appComponentConfigurations.getConfiguration(plugin.getDescriptor().getPluginId());
			((IConfigurationAware)extension).setConfiguration(configuration);
		}
		
		if (extension instanceof IApplicationComponentServiceAware) {
			((IApplicationComponentServiceAware)extension).setApplicationComponentService(this);
		}
		
		if (extension instanceof IInitializable) {
			((IInitializable)extension).init();
		}
		
		return extension;
	}

	@Override
	public void start() {
		if (!started) {
			pluginManager.loadPlugins();
			pluginManager.startPlugins();
			
			started = true;
		}
	}

	@Override
	public void stop() {
		if (started) {
			pluginManager.stopPlugins();
			pluginManager.unloadPlugins();
			
			started = false;
		}
	}

}
