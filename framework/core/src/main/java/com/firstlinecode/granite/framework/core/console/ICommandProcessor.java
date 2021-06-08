package com.firstlinecode.granite.framework.core.console;

import org.pf4j.ExtensionPoint;

public interface ICommandProcessor extends ExtensionPoint {
	public static final String DEFAULT_COMMAND_GROUP = "";
	
	String getGroup();
	String[] getCommands();
	void process(IConsoleSystem console, String command) throws Exception;
}
