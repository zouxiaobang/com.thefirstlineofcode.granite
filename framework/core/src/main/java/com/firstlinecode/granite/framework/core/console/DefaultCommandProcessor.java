package com.firstlinecode.granite.framework.core.console;

import org.pf4j.Extension;

@Extension
public class DefaultCommandProcessor extends AbstractCommandProcessor {

	@Override
	public String getGroup() {
		return ICommandProcessor.DEFAULT_COMMAND_GROUP;
	}
	
	@Override
	public String[] getCommands() {
		return new String[] {"help", "services", "service", "exit", "close"};
	}
	
	void processExit(IConsoleSystem consoleSystem) {
		consoleSystem.close();
	}
	
	void processClose(IConsoleSystem consoleSystem) {
		try {
			consoleSystem.getServerContext().getServer().stop();
		} catch (Exception e) {
			throw new RuntimeException("Can't stop server correctly.", e);
		}
		
		consoleSystem.close();
	}
	
	void processServices(IConsoleSystem consoleSystem) {
		System.out.println("TODO. List all services.");
	}
	
	void processService(IConsoleSystem consoleSystem, String serviceId) {
		System.out.println("TODO. Print service detail.");
	}
	
	void processHelp(IConsoleSystem consoleSystem) {
		consoleSystem.printMessageLine("Available Commands:");
		consoleSystem.printMessageLine("help                    Display help information.");
		consoleSystem.printMessageLine("services                List all started services.");
		consoleSystem.printMessageLine("service <SERVICE_ID>    Display details for specified service.");
		consoleSystem.printMessageLine("exit                    Exit the console.");
		consoleSystem.printMessageLine("close                   Stop the server and exit.");
	}
}
