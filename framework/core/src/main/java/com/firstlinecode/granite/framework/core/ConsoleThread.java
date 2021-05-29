package com.firstlinecode.granite.framework.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleThread implements Runnable {
	private volatile boolean stop = false;
	
	private IServer server;
	
	public ConsoleThread(IServer server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		printConsoleHelp();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				String command = readCommand(in);
				
				if (stop)
					break;
				
				if ("help".equals(command)) {
					printConsoleHelp();
				} else if ("exit".equals(command)) {
					exitSystem();
				} else {
					System.out.println(String.format("Unknown command: '%s'", command));
					printConsoleHelp();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exitSystem() {
		try {
			server.stop();			
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
		System.out.print("$");
	}
}
