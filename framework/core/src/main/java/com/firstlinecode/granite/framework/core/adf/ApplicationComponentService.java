package com.firstlinecode.granite.framework.core.adf;

import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.platform.IPluginManagerAware;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

public class ApplicationComponentService implements IApplicationComponentService {
	protected IServerConfiguration serverConfiguration;
	protected PluginManager pluginManager;
	protected IApplicationComponentConfigurations appComponentConfigurations;
	protected boolean syncPlugins;
	protected boolean started;
	
	public ApplicationComponentService(IServerConfiguration serverConfiguration) {
		this(serverConfiguration, null);
	}
	
	public ApplicationComponentService(IServerConfiguration serverConfiguration, PluginManager pluginManager) {
		this(serverConfiguration, pluginManager, true);
	}

	public ApplicationComponentService(IServerConfiguration serverConfiguration,
			PluginManager pluginManager, boolean syncPlugins) {
		this.serverConfiguration = serverConfiguration;
		this.syncPlugins = syncPlugins;
		appComponentConfigurations = readAppComponentConfigurations(serverConfiguration);
		
		if (pluginManager == null) {
			pluginManager = createPluginManager();
		} else {
			this.pluginManager = pluginManager;
		}
	}
	
	private ApplicationComponentConfigurations readAppComponentConfigurations(IServerConfiguration serverConfiguration) {
		return new ApplicationComponentConfigurations(serverConfiguration.getConfigurationDir());
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
		if (started)
			return;
		
		if (syncPlugins)
			initPlugins();
			
		started = true;
	}
	
	@Override
	public boolean isStarted() {
		return started;
	}

	private void initPlugins() {
		pluginManager.loadPlugins();
		pluginManager.startPlugins();
	}

	@Override
	public void stop() {
		if (!started)
			return;

		if (syncPlugins) {
			destroyPlugins();
		}
		
		started = false;
	}

	private void destroyPlugins() {
		pluginManager.stopPlugins();
		pluginManager.unloadPlugins();
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
