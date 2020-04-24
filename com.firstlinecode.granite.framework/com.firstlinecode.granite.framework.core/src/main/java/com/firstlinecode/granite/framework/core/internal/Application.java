package com.firstlinecode.granite.framework.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.IApplication;
import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.internal.repository.Repository;
import com.firstlinecode.granite.framework.core.internal.supports.ApplicationService;
import com.firstlinecode.granite.framework.core.platform.GraniteCommandProvider;
import com.firstlinecode.granite.framework.core.repository.IServiceListener;
import com.firstlinecode.granite.framework.core.repository.IServiceWrapper;

public class Application implements IApplication, IServiceListener {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	private IApplicationConfiguration appConfiguration;
	
	private BundleContext bundleContext;
	
	private Repository repository;
	
	private Map<String, IService> services;
	
	private ServiceRegistration<CommandProvider> srCommandProvider;
	
	private ApplicationService appService;
	
	public Application(BundleContext bundleContext, IApplicationConfiguration appConfiguration) {
		this.bundleContext = bundleContext;
		services = new HashMap<>();
		
		this.appConfiguration = appConfiguration;
	}

	@Override
	public void start() throws Exception {		
		repository = new Repository(bundleContext, appConfiguration);
		repository.addServiceListener(this);
		repository.init();
		
		appService = new ApplicationService(bundleContext, repository, appConfiguration);
		appService.start();	
		
		registerOsgiConsoleCommandProvider();
		
		logger.info("Granite runtime has started.");
	}

	private void registerOsgiConsoleCommandProvider() {
		srCommandProvider = bundleContext.registerService(CommandProvider.class,
				new GraniteCommandProvider(bundleContext, repository), null);
	}
	
	@Override
	public void stop() throws Exception {
		if (srCommandProvider != null)
			srCommandProvider.unregister();
		
		repository.destroy();
		
		appService.stop();
		
		logger.info("Granite runtime stopped.");
	}

	@Override
	public IApplicationConfiguration getConfiguration() {
		return appConfiguration;
	}

	@Override
	public void available(IServiceWrapper serviceWrapper) {
		for (String serviceId : appConfiguration.getDisabledServices()) {
			if (serviceWrapper.getId().equals(serviceId)) {
				logger.info("Serivce '{}' is available. But we don't start it because it's disabled.",
						serviceId);
				return;
			}
		}
		
		try {
			IService service = serviceWrapper.create();
			services.put(serviceWrapper.getId(), service);
			service.start();
			
			logger.info("Service '{}' has started.", serviceWrapper.getId());
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error(String.format("Can't start service %s.", serviceWrapper.getId()), e);
			}
		}
	}

	@Override
	public void unavailable(String serviceId) {
		IService service = services.get(serviceId);
		if (service != null) {
			try {
				service.stop();
				logger.info("Service '{}' stopped.", serviceId);
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error(String.format("Can't stop service '%s'.", serviceId), e);
				}
			}
		}
	}
	
}
