package com.firstlinecode.granite.framework.core.console;

import java.io.PrintStream;

import com.firstlinecode.granite.framework.core.IServerContext;

public interface IConsoleSystem {
	IServerContext getServerContext();
	ConsoleThread getConsoleThread();
	
	PrintStream getOutputStream();
	
	void printBlankLine();
	void printMessage(String message);
	void printMessageLine(String message);
}
