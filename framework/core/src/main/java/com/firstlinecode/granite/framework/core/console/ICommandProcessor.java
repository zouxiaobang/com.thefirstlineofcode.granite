package com.firstlinecode.granite.framework.core.console;

import org.pf4j.ExtensionPoint;

public interface ICommandProcessor extends ExtensionPoint {
	public static final String DEFAULT_COMMAND_GROUP = "";
	
	String getGroup();
	String[] getCommands();
	boolean process(IConsoleSystem console, String command, String... args) throws Exception;
}
