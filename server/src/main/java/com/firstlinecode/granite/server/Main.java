package com.firstlinecode.granite.server;

import java.net.URL;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.firstlinecode.granite.framework.adf.mybatis.AdfMybatisConfiguration;
import com.firstlinecode.granite.framework.adf.spring.AdfComponentService;
import com.firstlinecode.granite.framework.adf.spring.AdfPluginManager;
import com.firstlinecode.granite.framework.adf.spring.AdfSpringBeanPostProcessor;
import com.firstlinecode.granite.framework.adf.spring.ISpringConfiguration;
import com.firstlinecode.granite.framework.core.IServer;
import com.firstlinecode.granite.framework.core.ServerProxy;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.ServerConfiguration;
import com.firstlinecode.granite.framework.core.console.ConsoleSystem;
import com.firstlinecode.granite.framework.core.log.LogFilter;
import com.firstlinecode.granite.framework.core.platform.IPlatform;
import com.firstlinecode.granite.framework.core.platform.Pf4j;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class Main {
	public static void main(String[] args) {
		new Main().run(args);
	}
	
	private void run(String[] args) {
		Options options = null;
		try {
			options = parseOptions(args);
		} catch (IllegalArgumentException e) {
			if (e.getMessage() != null)
				System.out.println("Error: " + e.getMessage());
			printUsage();
			return;
		}
		
		if (options.isHelp()) {
			printUsage();
			return;
		}
		
		IServerConfiguration serverConfiguration = readServerConfiguration();
		
		if (options.getLogLevel() != null) {
			configureLog(options.getLogLevel(), serverConfiguration);
		} else {			
			configureLog(serverConfiguration.getLogLevel(), serverConfiguration);
		}
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		IServer server = null;
		AnnotationConfigApplicationContext appContext = null;
		AdfComponentService appComponentService = null;
		try {			
			AdfPluginManager pluginManager = new AdfPluginManager();
			
			appComponentService = new AdfComponentService(
					serverConfiguration, pluginManager);
			pluginManager.setApplicationComponentService(appComponentService);
			appComponentService.start();
			
			appContext = new AnnotationConfigApplicationContext();
						
			ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory)appContext.getBeanFactory();
			beanFactory.addBeanPostProcessor(new AdfSpringBeanPostProcessor(appComponentService));
			
			appContext.register(AdfMybatisConfiguration.class);
			
			List<Class<? extends ISpringConfiguration>> contributedSpringConfigurations =
					pluginManager.getExtensionClasses(ISpringConfiguration.class);
			appContext.register(contributedSpringConfigurations.toArray(
					new Class<?>[contributedSpringConfigurations.size()]));
			
			appContext.refresh();
			
			appComponentService.setApplicationContext(appContext);
			
			pluginManager.setApplicationContext(appContext);
			pluginManager.injectExtensionsToSpring();
			
			server = new ServerProxy().start(serverConfiguration, appComponentService);
		} catch (Exception e) {
			if (appContext != null)
				appContext.close();
			
			if (appComponentService != null && appComponentService.isStarted()) {
				appComponentService.stop();
			}
			
			if (server != null) {
				try {
					server.stop();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
			throw new RuntimeException("Can't start Granite Server.", e);
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Waiting a while. For that all services has started.
		}
		
		if (options.isConsole()) {
			Thread consoleThread = new Thread(new ConsoleSystem(server.getServerContext()),
					"Granite Server Console Thread");
			consoleThread.start();
		}
	}

	private void configureLog(String logLevel, IServerConfiguration serverConfiguration) {
		System.setProperty("granite.logs.dir", serverConfiguration.getLogsDir());
		
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		
		if (logLevel != null) {			
			if ("debug".equals(logLevel)) {
				configureLog(lc, "logback_debug.xml");
			} else if ("trace".equals(logLevel)) {
				configureLog(lc, "logback_trace.xml");
			} else if ("info".equals(logLevel)) {
				configureLog(lc, "logback.xml");
			} else {
				throw new IllegalArgumentException("Unknown log level option. Only 'info', 'debug' or 'trace' is supported.");
			}
		} else {
			configureLog(lc, "logback.xml");
		}
		
		lc.addTurboFilter(new LogFilter(serverConfiguration.getApplicationLogNamespaces(),
				serverConfiguration.isThirdpartyLogEnabled()));
	}

	private void configureLog(LoggerContext lc, String logFile) {
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
	
	private IServerConfiguration readServerConfiguration() {
		IPlatform platform = new Pf4j();
		String serverHome = platform.getHomeDirectory();
		
		if (serverHome == null) {
			throw new RuntimeException("Can't determine granite server home.");
		}
		
		return new ServerConfiguration(serverHome);
	}
	
	private Options parseOptions(String[] args) {
		Options options = new Options();
		
		if (args.length == 1 && args[0].equals("-help")) {
			options.setHelp(true);
			
			return options;
		}
		
		int i = 0;
		while (i < args.length) {
			if ("-console".equals(args[i])) {
				options.setConsole(true);
				i++;
			} else if ("-logLevel".equals(args[i])) {
				if ("info".equals(args[i]) || "debug".equals(args[i]) || "trace".equals(args[i])) {
					options.setLogLevel(args[i]);
				} else {
					throw new IllegalArgumentException("Unknown log level. Only 'info', 'debug', or 'trace' supported. Default is 'info'.");
				}
				i++;
			} else if ("-help".equals(args[i])) {
				throw new IllegalArgumentException("-help should be used alonely.");
			} else {
				throw new IllegalArgumentException(String.format("Unknown option: %s", args[i]));
			}
		}
		
		return options;
	}
	
	private void printUsage() {
		System.out.println("Usage:");
		System.out.println("java -jar com.firstlinecode.granite.server-${VERSION}.jar [OPTIONS]");
		System.out.println("OPTIONS:");
		System.out.println("-help                            Display help information.");
		System.out.println("-console                         Start the server with console.");
		System.out.println("-logLevel LOG_LEVEL              Set log level for the server. Three options allowed: 'info', 'debug', 'trace'. Default is 'info'.");
	}
}
