package com.firstlinecode.granite.framework.core;

import com.firstlinecode.granite.framework.core.adf.IApplicationComponentConfigurations;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.repository.IRepository;

public interface IServerContext {
	IServer getServer();
	IServerConfiguration getServerConfiguration();
	IRepository getRepository();
	IApplicationComponentConfigurations getApplicationComponentConfigurations();
	IApplicationComponentService getApplicationComponentService();
}
