package com.firstlinecode.granite.framework.adf.spring;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.firstlinecode.granite.framework.adf.spring.injection.SpringBeanInjectionProvider;
import com.firstlinecode.granite.framework.core.adf.ApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.data.IDataObjectFactory;
import com.firstlinecode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.firstlinecode.granite.framework.core.adf.injection.AppComponentInjectionProvider;
import com.firstlinecode.granite.framework.core.adf.injection.IInjectionProvider;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.utils.CompositeEnumeration;

public class AdfComponentService extends ApplicationComponentService {
	protected AnnotationConfigApplicationContext appContext;
	protected IDataObjectFactory dataObjectFactory;
	
	public AdfComponentService(IServerConfiguration serverConfiguration) {
		super(serverConfiguration);
		
		appContext = new AnnotationConfigApplicationContext();
		
		ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory)appContext.getBeanFactory();
		beanFactory.addBeanPostProcessor(new AdfSpringBeanPostProcessor(this));
		
		registerPredefinedSpringConfigurations();
		
		ClassLoader[] classLoaders = registerContributedSpringConfigurations();
		if (classLoaders != null) {			
			appContext.setClassLoader(new CompositeClassLoader(classLoaders));
		}
		
		appContext.refresh();
		
		AdfPluginManager adfPluginManager = (AdfPluginManager)pluginManager;
		adfPluginManager.setApplicationContext(appContext);
		adfPluginManager.injectExtensionsToSpring();
	}
	
	private class CompositeClassLoader extends ClassLoader {
		private ClassLoader[] classLoaders;
		
		public CompositeClassLoader(ClassLoader[] classLoaders) {
			if (classLoaders == null || classLoaders.length == 0)
				throw new IllegalArgumentException("Null class loaders or no any class loader.");
			
			this.classLoaders = classLoaders;
		}
		
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			for (ClassLoader classLoader : classLoaders) {
				try {
					Class<?> clazz = classLoader.loadClass(name);
					if (clazz != null)
						return clazz;
				} catch (ClassNotFoundException e) {
					// Ignore
				}
			}
			
			throw new ClassNotFoundException(name);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			Enumeration<URL>[] allResources = (Enumeration<URL>[])Array.newInstance(Enumeration.class, classLoaders.length);
			for (int i = 0; i < classLoaders.length; i++) {
				allResources[i] = classLoaders[i].getResources(name);
			}
			
			return new CompositeEnumeration<URL>(allResources);
		}
		
		@Override
		public URL getResource(String name) {
			for (ClassLoader classLoader : classLoaders) {
				URL url = classLoader.getResource(name);
				if (url != null)
					return url;
			}
			
			return null;
		}
	}
	
	public ApplicationContext getApplicationContext() {
		return appContext;
	}

	private ClassLoader[] registerContributedSpringConfigurations() {
		List<Class<? extends ISpringConfiguration>> contributedSpringConfigurationClasses =
				pluginManager.getExtensionClasses(ISpringConfiguration.class);
		if (contributedSpringConfigurationClasses == null || contributedSpringConfigurationClasses.size() == 0)
			return null;
		
		List<ClassLoader> classLoaders = new ArrayList<>();
		for (Class<? extends ISpringConfiguration> contributedSpringConfigurationClass : contributedSpringConfigurationClasses) {			
			appContext.register(contributedSpringConfigurationClasses.toArray(
					new Class<?>[contributedSpringConfigurationClasses.size()]));
			
			classLoaders.add(contributedSpringConfigurationClass.getClassLoader());
		}
		
		return classLoaders.toArray(new ClassLoader[classLoaders.size()]);
	}
	
	protected void registerPredefinedSpringConfigurations() {}

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
		
		if (injectAppContext) {
			injectedInstance = injectAppContext(injectAppContext, injectedInstance);
		}
		
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
	
	@Override
	protected <T> void injectByAwareInterfaces(T instance) {
		super.injectByAwareInterfaces(instance);
		injectDataObjectFactory(instance);
	}
	
	private void injectDataObjectFactory(Object instance) {
		if (!(instance instanceof IDataObjectFactoryAware))
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
		
		((IDataObjectFactoryAware)instance).setDataObjectFactory(dataObjectFactory);
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
