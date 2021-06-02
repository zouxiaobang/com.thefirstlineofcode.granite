package com.firstlinecode.granite.framework.core;

import com.firstlinecode.granite.framework.core.app.IApplicationComponentConfigurations;
import com.firstlinecode.granite.framework.core.app.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.repository.IRepository;

public interface IServerContext {
	IServerConfiguration getServerConfiguration();
	IRepository getRepository();
	IApplicationComponentConfigurations getApplicationComponentConfigurations();
	IApplicationComponentService getApplicationComponentService();
}
