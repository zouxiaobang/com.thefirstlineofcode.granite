package com.firstlinecode.granite.framework.core.console;

import java.io.PrintStream;

import com.firstlinecode.granite.framework.core.IServerContext;

public class ConsoleSystem implements IConsoleSystem {
	private IServerContext serverContext;
	private ConsoleThread consoleThread;
	
	public ConsoleSystem(IServerContext serverContext, ConsoleThread consoleThread) {
		this.serverContext = serverContext;
		this.consoleThread = consoleThread;
	}
	
	@Override
	public IServerContext getServerContext() {
		return serverContext;
	}

	@Override
	public ConsoleThread getConsoleThread() {
		return consoleThread;
	}

	@Override
	public void printBlankLine() {
		System.out.println();
	}

	@Override
	public void printMessage(String message) {
		System.out.print(message);
	}

	@Override
	public PrintStream getOutputStream() {
		return System.out;
	}

	@Override
	public void printMessageLine(String message) {
		System.out.println(message);
	}

}
