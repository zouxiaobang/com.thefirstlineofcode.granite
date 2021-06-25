package com.firstlinecode.granite.framework.adf.spring;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.pf4j.JarPluginLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.CompoundEnumeration;

public class AdfPluginLoader extends JarPluginLoader {
	private static final Logger logger = LoggerFactory.getLogger(AdfPluginLoader.class);
	
	public AdfPluginLoader(AdfPluginManager pluginManager) {
		super(pluginManager);
	}
	
	@Override
	public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {		
		String pluginId = pluginDescriptor.getPluginId();
		String[] nonPluginDependencyIds = ((AdfPluginManager)pluginManager).getNonPluginDependencyIdsByPluginId(pluginId);
		if (nonPluginDependencyIds == null || nonPluginDependencyIds.length == 0)
			return super.loadPlugin(pluginPath, pluginDescriptor);
		
		URL[] nonPluginDependencies = getDependenciesByIds(pluginId, nonPluginDependencyIds);
		if (nonPluginDependencies.length == 0)
			return super.loadPlugin(pluginPath, pluginDescriptor);
		
		PluginClassLoader adfClassLoader = new AdfClassLoader(pluginManager, pluginDescriptor,
				getClass().getClassLoader(), nonPluginDependencies);
		adfClassLoader.addFile(pluginPath.toFile());
		
		return adfClassLoader;
	}

	private URL[] getDependenciesByIds(String pluginId, String[] nonPluginDependencyIds) {
		List<URL> lDependencies = new ArrayList<>();
		for (String nonPluginDependencyId : nonPluginDependencyIds) {
			URL dependency = findDependencyByDependencyId(nonPluginDependencyId);
			
			if (dependency != null) {
				lDependencies.add(dependency);
			} else {
				logger.warn(String.format("Non-plugin dependency which's id is '%s' not be found. It's needed by plugin: %s.",
						nonPluginDependencyId, pluginId));
			}
		}
		
		return lDependencies.toArray(new URL[lDependencies.size()]);
	}

	private URL findDependencyByDependencyId(String nonPluginDependencyId) {
		URL[] nonPluginDependencies = ((AdfPluginManager)pluginManager).getNonPluginDependencies();
		for (URL nonPluginDependency : nonPluginDependencies) {
			if (nonPluginDependency.getFile().contains(nonPluginDependencyId)) {
				return nonPluginDependency;
			}
		}
		
		return null;
	}
	
	private class AdfClassLoader extends PluginClassLoader {
		private URLClassLoader nonPluginDependenciesClassLoader;
		
		public AdfClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor,
				ClassLoader parent, URL[] nonPluginDependencies) {
			super(pluginManager, pluginDescriptor, parent);
			
			nonPluginDependenciesClassLoader = new URLClassLoader(nonPluginDependencies);
		}
		
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			try {				
				return super.loadClass(name);
			} catch (ClassNotFoundException e) {
				// Ignore. Try to load class from non-plugin dependencies.
			}
			
			return nonPluginDependenciesClassLoader.loadClass(name);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			Enumeration<URL> pluginUrls = super.getResources(name);
			Enumeration<URL> nonPluginDependencyUrls = nonPluginDependenciesClassLoader.getResources(name);
			
			Enumeration<URL>[] urls = new Enumeration[2];
			urls[0] = pluginUrls;
			urls[1] = nonPluginDependencyUrls;
			
			return new CompoundEnumeration<>(urls);
		}
		
		@Override
		public URL getResource(String name) {
			URL url = super.getResource(name);
			if (url != null && url.getFile().indexOf("pf4j-spring") != -1 &&
					url.getFile().indexOf("META-INF/extensions.idx") != -1) {
				return null;
			}
			
			if (url == null) {
				url = nonPluginDependenciesClassLoader.getResource(name);
			}
			
			return url;
		}
	}
}
