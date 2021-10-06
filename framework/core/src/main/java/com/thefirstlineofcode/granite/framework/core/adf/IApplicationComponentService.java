package com.thefirstlineofcode.granite.framework.core.adf;

import java.util.List;

import org.pf4j.PluginManager;

public interface IApplicationComponentService {
	void start();
	void stop();
	boolean isStarted();
	<T> List<Class<? extends T>> getExtensionClasses(Class<T> type);
	<T> T createExtension(Class<T> type);
	<T> T createRawExtension(Class<T> type);
	<T> T inject(T rawInstance);
	<T> T getAppComponent(String id, Class<T> type);
	PluginManager getPluginManager();
	IPluginConfigurations getPluginConfigurations();
}
