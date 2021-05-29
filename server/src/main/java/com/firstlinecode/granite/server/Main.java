package com.firstlinecode.granite.server;

import java.net.URL;

import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.ConsoleThread;
import com.firstlinecode.granite.framework.core.IServer;
import com.firstlinecode.granite.framework.core.ServerProxy;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.ServerConfiguration;
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
		configureLog(serverConfiguration.getLogLevel(), serverConfiguration.isThirdpartyLogEnabled(), serverConfiguration.getApplicationLogNamespaces());
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		ServerProxy serverProxy = new ServerProxy();
		IServer server = serverProxy.start(serverConfiguration);
		
		if (options.isConsole()) {
			Thread consoleThread = new Thread(new ConsoleThread(server), "Granite Server Console Thread");
			consoleThread.start();
		}
	}
	
	private void configureLog(String logLevel, boolean enableThidpartyLogs, String[] applicationNamespaces) {
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		
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
		
		lc.addTurboFilter(new LogFilter(applicationNamespaces, enableThidpartyLogs));
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
