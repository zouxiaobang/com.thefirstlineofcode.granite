package com.firstlinecode.granite.framework.adf.spring;

import java.net.URL;
import java.nio.file.Path;

import org.pf4j.JarPluginLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

public class AdfPluginLoader extends JarPluginLoader {
	private URL[] nonPluginDependencies;

	public AdfPluginLoader(PluginManager pluginManager, URL[] nonPluginDependencies) {
		super(pluginManager);
		
		this.nonPluginDependencies = nonPluginDependencies;
	}
	
	@Override
	public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
		// TODO Auto-generated method stub
		//ClassLoader pluginClassLoader = super.loadPlugin(pluginPath, pluginDescriptor);
		return super.loadPlugin(pluginPath, pluginDescriptor);
	}
}
