package com.firstlinecode.granite.framework.adf.spring;

import com.firstlinecode.granite.framework.core.Server;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;

public class AdfServer extends Server {

	public AdfServer(IServerConfiguration configuration) {
		super(configuration);
	}
	
	protected IApplicationComponentService createAppComponentService() {
		return new AdfComponentService(configuration);
	}

}
