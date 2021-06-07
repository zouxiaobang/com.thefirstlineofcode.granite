package com.firstlinecode.granite.framework.adf.spring;

import org.pf4j.PluginManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.firstlinecode.granite.framework.adf.spring.injection.SpringBeanInjectionProvider;
import com.firstlinecode.granite.framework.core.adf.ApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.injection.AppComponentInjectionProvider;
import com.firstlinecode.granite.framework.core.adf.injection.IInjectionProvider;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public class AdfComponentService extends ApplicationComponentService implements ApplicationContextAware {
	private ApplicationContext appContext;

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
	
	@Override
	public <T> T inject(T rawInstance) {
		return inject(rawInstance, true);
	}
	
	public <T> T inject(T rawInstance, boolean injectAppContext) {
		T injectedInstance = super.inject(rawInstance);
		
		if (!injectAppContext)
			return injectedInstance;
		
		if (injectedInstance instanceof ApplicationContextAware) {
			((ApplicationContextAware)injectedInstance).setApplicationContext(appContext);
		}
		
		return injectedInstance;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext = applicationContext;
	}
	
	@Override
	protected IInjectionProvider[] getInjectionProviders() {
		return new IInjectionProvider[] {new AppComponentInjectionProvider(this), new SpringBeanInjectionProvider(appContext)};
	}
}
