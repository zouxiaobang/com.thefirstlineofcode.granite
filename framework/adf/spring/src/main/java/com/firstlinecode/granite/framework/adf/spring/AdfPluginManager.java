package com.firstlinecode.granite.framework.adf.spring;

import java.nio.file.Path;

import org.pf4j.DefaultPluginFactory;
import org.pf4j.ExtensionFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.ExtensionsInjector;
import org.pf4j.spring.SpringExtensionFactory;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationContextAware;

import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;

public class AdfPluginManager extends SpringPluginManager implements ApplicationContextAware,
			IApplicationComponentServiceAware {
	private AdfComponentService appComponentService;
	
	public AdfPluginManager() {
	}
	
	public AdfPluginManager(Path pluginsRoot) {
		super(pluginsRoot);
	}
	
	@Override
	protected PluginFactory createPluginFactory() {
		return new AdfPluginFactory();
	}
	
	private class AdfPluginFactory extends DefaultPluginFactory {
		@Override
		public Plugin create(PluginWrapper pluginWrapper) {
			Plugin plugin = super.create(pluginWrapper);
			if (plugin == null)
				return null;
			
			plugin = appComponentService.inject(plugin);
			if (plugin instanceof ApplicationContextAware) {
				((ApplicationContextAware)plugin).setApplicationContext(getApplicationContext());
			}
			
			return plugin;
		}
	}
	
	@Override
	protected ExtensionFactory createExtensionFactory() {
		return new AdfExtensionFactory(this);
	}
	
	private class AdfExtensionFactory extends SpringExtensionFactory {

		public AdfExtensionFactory(PluginManager pluginManager) {
			super(pluginManager);
		}
		
		@Override
		public <T> T create(Class<T> extensionClass) {
			T extension = super.create(extensionClass);
			if (extension != null)
				appComponentService.inject(extension);
			
			return extension;
		}
	}
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		if (appComponentService instanceof AdfComponentService) {
			this.appComponentService = (AdfComponentService)appComponentService;
		} else {
			throw new IllegalArgumentException(String.format("Class %s Not an ADF plugin manager.", appComponentService.getClass().getName()));
		}
	}
	
	public void init() {
		// Override super.init(). Do nothing.
	}
	
	public void injectExtensionsToSpring() {
		AbstractAutowireCapableBeanFactory beanFactory = (AbstractAutowireCapableBeanFactory)getApplicationContext().getAutowireCapableBeanFactory();
		ExtensionsInjector extensionsInjector = new ExtensionsInjector(this, beanFactory);
		extensionsInjector.injectExtensions();
	}
}
