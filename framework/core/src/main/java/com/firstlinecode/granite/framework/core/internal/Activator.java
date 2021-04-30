package com.firstlinecode.granite.framework.core.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.internal.config.ApplicationConfiguration;
import com.firstlinecode.granite.framework.core.log.LogFilter;
import com.firstlinecode.granite.framework.core.platform.IPlatform;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class Activator implements BundleActivator {
	private ApplicationProxy applicationProxy;

	
	@Override
	public void start(BundleContext context) throws Exception {
		IApplicationConfiguration appConfiguration = createAppConfiguration(context);
		
		System.setProperty("granite.app.home", appConfiguration.getAppHome());
		System.setProperty("granite.config.dir", appConfiguration.getConfigDir());
		System.setProperty("granite.logs.dir", appConfiguration.getConfigDir() + "/logs");
		configureLog(appConfiguration.getLogConfigurationFile(), appConfiguration.getApplicationNamespaces());
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		applicationProxy = new ApplicationProxy();
		applicationProxy.start(context, appConfiguration);
	}
	
	protected IApplicationConfiguration createAppConfiguration(BundleContext context) {
		IPlatform platform = OsgiUtils.getPlatform(context);
		URL appHome = platform.getHomeDirectory();
		
		File fGraniteConfigDir = OsgiUtils.getGraniteConfigDir(context);
		
		if (fGraniteConfigDir == null) {
			throw new RuntimeException("Can't determine granite configuration directory.");
		}
		
		return new ApplicationConfiguration(appHome.getPath(), fGraniteConfigDir.getPath());
	}
	
	private void configureLog(String logConfigurationFile, String[] applicationNamespaces) {
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		
		if (logConfigurationFile != null) {
			File fLogConfigurationFile = new File(logConfigurationFile);
			if (!fLogConfigurationFile.exists())
				throw new RuntimeException(String.format("Log configuration file doesn't exist: %s.",
						logConfigurationFile));
			
			try {
				configureLC(lc, fLogConfigurationFile.toURI().toURL());
			} catch (MalformedURLException e) {
				throw new RuntimeException("Can't configure log", e);
			}
			
			return;
		}
		
		String logLevel = System.getProperty("granite.log.level");
		
		if (logLevel != null) {			
			if ("debug".equals(logLevel)) {
				configureSystemLogFile(lc, "logback_debug.xml");
			} else if ("trace".equals(logLevel)) {
				configureSystemLogFile(lc, "logback_trace.xml");
			} else if ("info".equals(logLevel)) {
				configureSystemLogFile(lc, "logback.xml");
			} else {
				throw new IllegalArgumentException("Unknown log level option. Only 'info', 'debug' or 'trace' is supported.");
			}
		} else {
			configureSystemLogFile(lc, "logback.xml");
		}
		
		lc.addTurboFilter(new LogFilter(applicationNamespaces));
	}

	private void configureSystemLogFile(LoggerContext lc, String logFile) {
		configureLC(lc, getClass().getClassLoader().getResource(logFile));
	}

	private void configureLC(LoggerContext lc, URL url) {
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			lc.reset();
			configurator.setContext(lc);
			configurator.doConfigure(url);
		} catch (JoranException e) {
			// ignore, StatusPrinter will handle this
		}
		
	    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		applicationProxy.stop(context);
	}

}
