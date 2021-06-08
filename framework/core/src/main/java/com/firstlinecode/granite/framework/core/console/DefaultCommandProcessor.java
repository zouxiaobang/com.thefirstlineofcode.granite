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
		return new String[] {"help", "exit"};
	}
	
	void processExit(IConsoleSystem consoleSystem) {
		try {
			consoleSystem.getServerContext().getServer().stop();
		} catch (Exception e) {
			throw new RuntimeException("Can't stop server correctly.", e);
		}
		
		consoleSystem.getConsoleThread().stop();
	}
	
	void processHelp(IConsoleSystem consoleSystem) {
		consoleSystem.printMessageLine("Commands:");
		consoleSystem.printMessageLine("help        Display help information.");
		consoleSystem.printMessageLine("exit        Exit system.");
	}
}
