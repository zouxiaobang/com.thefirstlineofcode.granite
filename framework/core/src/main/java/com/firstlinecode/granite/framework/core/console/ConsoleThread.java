package com.firstlinecode.granite.framework.core.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.firstlinecode.granite.framework.core.IServerContext;

public class ConsoleThread implements Runnable {
	private volatile boolean stop = false;
	
	private IServerContext serverContext;
	
	public ConsoleThread(IServerContext serverContext) {
		this.serverContext = serverContext;
	}
	
	@Override
	public void run() {
		printBlankLine();
		printConsoleHelp();
		printBlankLine();
		printPrompt();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				if (stop)
					break;
				
				String command = readCommand(in);
				
				printBlankLine();
				processCommand(command);
				printBlankLine();
				
				printPrompt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printBlankLine() {
		System.out.println();
	}

	private void processCommand(String command) {
		// TODO Auto-generated method stub
		if ("help".equals(command)) {
			printConsoleHelp();
		} else if ("exit".equals(command)) {
			exitSystem();
		} else {
			System.out.println(String.format("Unknown command: '%s'", command));
			printBlankLine();
			printConsoleHelp();
		}
	}

	private void exitSystem() {
		try {
			serverContext.getServer().stop();
		} catch (Exception e) {
			throw new RuntimeException("Can't stop server correctly.", e);
		}
		stop = true;
	}

	private String readCommand(BufferedReader in) throws IOException {
		while (!in.ready()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (stop) {
				return null;
			}
		}
		
		return in.readLine();
	}

	private void printConsoleHelp() {
		System.out.println("Commands:");
		System.out.println("help        Display help information.");
		System.out.println("exit        Exit system.");
	}
	
	private void printPrompt() {
		System.out.print("$");		
	}
}
