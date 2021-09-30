package com.thefirstlineofcode.granite.framework.core;

import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentConfigurations;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.repository.IRepository;

public interface IServerContext {
	IServer getServer();
	IServerConfiguration getServerConfiguration();
	IRepository getRepository();
	IApplicationComponentConfigurations getApplicationComponentConfigurations();
	IApplicationComponentService getApplicationComponentService();
}
