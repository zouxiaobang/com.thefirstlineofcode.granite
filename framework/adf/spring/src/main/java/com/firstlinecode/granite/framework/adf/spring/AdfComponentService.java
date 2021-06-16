package com.firstlinecode.granite.framework.adf.spring;

import org.pf4j.PluginManager;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

import com.firstlinecode.granite.framework.adf.spring.injection.SpringBeanInjectionProvider;
import com.firstlinecode.granite.framework.core.adf.ApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.data.IDataObjectFactory;
import com.firstlinecode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.firstlinecode.granite.framework.core.adf.injection.AppComponentInjectionProvider;
import com.firstlinecode.granite.framework.core.adf.injection.IInjectionProvider;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public class AdfComponentService extends ApplicationComponentService implements ApplicationContextAware {
	private ApplicationContext appContext;
	private IDataObjectFactory dataObjectFactory;
	
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
		
		injectDataObjectFactory(injectedInstance);
		injectedInstance = injectAppContext(injectAppContext, injectedInstance);
		
		return injectedInstance;
	}

	private <T> T injectAppContext(boolean injectAppContext, T injectedInstance) {
		if (!injectAppContext)
			return injectedInstance;
		
		if (injectedInstance instanceof ApplicationContextAware) {
			((ApplicationContextAware)injectedInstance).setApplicationContext(appContext);
		}
		
		return injectedInstance;
	}

	private void injectDataObjectFactory(Object injectedInstance) {
		if (!(injectedInstance instanceof IDataObjectFactoryAware))
			return;
			
		if (dataObjectFactory == null) {
			synchronized (this) {
				if (dataObjectFactory == null)
					dataObjectFactory = (IDataObjectFactory)getAppComponent(
							IDataObjectFactory.COMPONENT_ID_DATA_OBJECT_FACTORY,
							IDataObjectFactory.class);
			}
		}
		
		if (dataObjectFactory == null)
			throw new RuntimeException("Can't find a data object factory to do application component injection.");
		
		((IDataObjectFactoryAware)injectedInstance).setDataObjectFactory(dataObjectFactory);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext = applicationContext;
	}
	
	@Override
	protected IInjectionProvider[] getInjectionProviders() {
		return new IInjectionProvider[] {new AppComponentInjectionProvider(this), new SpringBeanInjectionProvider(appContext)};
	}
	
	@Override
	public void stop() {
		AbstractApplicationContext appContext = (AbstractApplicationContext)((SpringPluginManager)pluginManager).getApplicationContext();
		if (appContext != null)
			appContext.close();
		
		super.stop();
	}
}
