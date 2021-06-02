package com.firstlinecode.granite.framework.core.app;

import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.platform.AppComponentPluginManager;
import com.firstlinecode.granite.framework.core.platform.IPluginManagerAware;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

public class ApplicationComponentService implements IApplicationComponentService {
	private IServerConfiguration serverConfiguration;
	private PluginManager pluginManager;
	private IApplicationComponentConfigurations appComponentConfigurations;
	private boolean started;
	
	public ApplicationComponentService(IServerConfiguration serverConfiguration,
			IApplicationComponentConfigurations appComponentConfigurations) {
		this(serverConfiguration, appComponentConfigurations, null);
	}

	public ApplicationComponentService(IServerConfiguration serverConfiguration,
			IApplicationComponentConfigurations appComponentConfigurations,
			PluginManager pluginManager) {
		this.serverConfiguration = serverConfiguration;
		this.appComponentConfigurations = appComponentConfigurations;
		
		if (pluginManager == null) {
			pluginManager = createPluginManager();
		} else {
			this.pluginManager = pluginManager;
		}
	}
	
	protected PluginManager createPluginManager() {
		return new AppComponentPluginManager(this);
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
		T extension = createRawExtension(type);
		
		return inject(extension);
	}
	
	@Override
	public <T> T createRawExtension(Class<T> type) {
		PluginWrapper plugin = pluginManager.whichPlugin(type);
		if (plugin == null)
			throw new IllegalArgumentException("Can't determine the class %s is loaded from which plugin.");
		
		T extension = null;
		try {
			extension = type.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't create extension which's type is %s", type.getName()), e);
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

	@Override
	public <T> T inject(T rawInstance) {
		if (rawInstance instanceof IServerConfigurationAware) {
			((IServerConfigurationAware)rawInstance).setServerConfiguration(serverConfiguration);
		}
		
		if (rawInstance instanceof IConfigurationAware) {
			Class<?> type = rawInstance.getClass();
			PluginWrapper plugin = pluginManager.whichPlugin(type);
			if (plugin == null)
				throw new IllegalArgumentException(
					String.format("Can't determine which plugin the extension which's class name is %s is load from.", type));
			
			IConfiguration configuration = appComponentConfigurations.getConfiguration(plugin.getDescriptor().getPluginId());
			((IConfigurationAware)rawInstance).setConfiguration(configuration);
		}
		
		if (rawInstance instanceof IPluginManagerAware) {
			((IPluginManagerAware)rawInstance).setPluginManager(pluginManager);
		}
		
		if (rawInstance instanceof IApplicationComponentServiceAware) {
			((IApplicationComponentServiceAware)rawInstance).setApplicationComponentService(this);
		}
		
		if (rawInstance instanceof IInitializable) {
			((IInitializable)rawInstance).init();
		}
		
		return rawInstance;
	}

}
