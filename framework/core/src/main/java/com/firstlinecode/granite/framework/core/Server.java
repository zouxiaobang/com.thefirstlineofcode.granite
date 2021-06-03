package com.firstlinecode.granite.framework.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.adf.ApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.repository.IServiceListener;
import com.firstlinecode.granite.framework.core.repository.IServiceWrapper;
import com.firstlinecode.granite.framework.core.repository.Repository;
import com.firstlinecode.granite.framework.core.repository.ServiceCreationException;

public class Server implements IServer, IServiceListener {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
		
	private IServerConfiguration configuration;
	
	private IApplicationComponentService appComponentService;
	private IRepository repository;
	private Map<String, IService> services;
		
	public Server(IServerConfiguration configuration, IApplicationComponentService appComponentService) {
		this.configuration = configuration;
		this.appComponentService = appComponentService;
		services = new HashMap<>();
	}

	@Override
	public void start() throws Exception {
		appComponentService.start();
		
		repository = new Repository(configuration, appComponentService);
		repository.init();
		
		logger.info("Granite Server has Started");
	}

	@Override
	public void stop() throws Exception {
		for (Map.Entry<String, IService> entry : services.entrySet()) {
			try {
				entry.getValue().stop();
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error("Can't stop service which's ID is {}.", entry.getKey(), e);
				}
			}
		}
		
		appComponentService.stop();
		
		logger.info("Granite Server has stopped.");
	}

	@Override
	public IServerContext getServerContext() {
		return new ServerContext(this, repository, (ApplicationComponentService)appComponentService);
	}

	@Override
	public void available(IServiceWrapper serviceWrapper) {
		try {
			createAndRunService(serviceWrapper);
		} catch (ServiceCreationException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Can't create service which's ID is {}.", serviceWrapper.getId(), e);
			}
		}
	}

	private void createAndRunService(IServiceWrapper serviceWrapper) throws ServiceCreationException {
		IService service = serviceWrapper.create();
		services.put(serviceWrapper.getId(), service);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					service.start();
				} catch (Exception e) {
					if (logger.isErrorEnabled()) {
						logger.error("Can't start service which's ID is {}.", serviceWrapper.getId(), e);
					}
				}
			}
		}).start();;
	}

	@Override
	public IServerConfiguration getConfiguration() {
		return configuration;
	}
}
