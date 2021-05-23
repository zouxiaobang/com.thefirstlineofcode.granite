package com.firstlinecode.granite.framework.core;

import org.pf4j.PluginManager;

import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.repository.IRepository;

public interface IServerContext {
	IServerConfiguration getServerConfiguration();
	IRepository getRepository();
	PluginManager getPluginManager();
}
