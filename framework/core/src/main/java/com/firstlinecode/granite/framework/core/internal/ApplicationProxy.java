package com.firstlinecode.granite.framework.core.internal;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.IApplication;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;

public class ApplicationProxy {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationProxy.class);
	
	private IApplication application;
	private ServiceRegistration<IApplicationConfiguration> srAppConfiguration;
	
	public void start(BundleContext context, IApplicationConfiguration appConfiguration) {
		srAppConfiguration = context.registerService(IApplicationConfiguration.class, appConfiguration, null);
		
		application = new Application(context, appConfiguration);
		try {
			application.start();
		} catch (Exception e) {
			logger.error("Failed to start granite runtime.", e);
		}
	}
	
	public void stop(BundleContext context) {
		try {
			if (srAppConfiguration != null)
				srAppConfiguration.unregister();
		} catch (Exception e) {
			logger.warn("Can't unregister application configuration service.", e);
		}
		
		try {
			application.stop();
		} catch (Exception e) {
			logger.error("Failed to stop granite runtime.", e);
		}
	}
}
