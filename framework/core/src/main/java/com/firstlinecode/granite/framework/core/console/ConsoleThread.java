package com.firstlinecode.granite.framework.core.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pf4j.PluginManager;

import com.firstlinecode.granite.framework.core.IServerContext;
import com.firstlinecode.granite.framework.core.adf.ApplicationComponentService;

public class ConsoleThread implements Runnable {
	private static final char CHAR_COLON = ':';

	private volatile boolean stop = false;
	
	private IServerContext serverContext;
	private IConsoleSystem consoleSystem;
	private Map<String, ICommandProcessor> commandProcessors;
	
	public ConsoleThread(IServerContext serverContext) {
		this.serverContext = serverContext;
		consoleSystem = new ConsoleSystem(serverContext, this);
		commandProcessors = new HashMap<>();
	}
	
	@Override
	public void run() {
		loadCommandProcessors();
		
		consoleSystem.printBlankLine();
		printDefaultHelp();
		consoleSystem.printBlankLine();
		printPrompt();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				if (stop)
					break;
				
				String command = readCommand(in);
				
				consoleSystem.printBlankLine();
				if (!processCommand(consoleSystem, command)) {
					consoleSystem.printBlankLine();
					printDefaultHelp();
				}
				
				consoleSystem.printBlankLine();
				printPrompt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printDefaultHelp() {
		try {
			doPrintDefaultHelp();
		} catch (Exception e) {
			consoleSystem.printMessageLine("Some things was wrong. Exit system now.");
			try {
				exitSystem();
			} catch (Exception e1) {
				throw new RuntimeException("Can't exit system.", e);
			}
		}
	}

	private void exitSystem() throws Exception {
		getDefaultCommandProcessor().process(consoleSystem, "exit");
	}

	private ICommandProcessor getDefaultCommandProcessor() {
		return commandProcessors.get(ICommandProcessor.DEFAULT_COMMAND_GROUP);
	}

	private void loadCommandProcessors() {
		ApplicationComponentService appComponentService = (ApplicationComponentService)serverContext.getApplicationComponentService();
		PluginManager pluginManager = appComponentService.getPluginManager();
		List<? extends ICommandProcessor> commandProcessorExtensions = pluginManager.getExtensions(ICommandProcessor.class);
		for (ICommandProcessor commandProcessorExtension : commandProcessorExtensions) {
			String group = commandProcessorExtension.getGroup();
			
			if (group == null) {
				throw new IllegalArgumentException("Null command group.");
			}
			
			if (commandProcessors.containsKey(group)) {
				throw new IllegalArgumentException(String.format("Reduplicated command group: '%s'.", group));
			}
			
			commandProcessors.put(group, commandProcessorExtension);
		}
	}

	private boolean processCommand(IConsoleSystem consoleSystem, String command) {
		int colonIndex = command.indexOf(CHAR_COLON);
		
		String group;
		if (colonIndex == -1) {
			// Default command group
			group = ICommandProcessor.DEFAULT_COMMAND_GROUP;
		} else {
			group = command.substring(0, colonIndex);
			command = command.substring(colonIndex, command.length());
		}
		
		ICommandProcessor commandProcessor = commandProcessors.get(group);
		if (commandProcessor == null) {
			consoleSystem.printMessageLine(String.format("Unknown command group: '%s'", group));
			
			return false;
		}
		
		for (String aCommand : commandProcessor.getCommands()) {
			if (aCommand.equals(command)) {
				try {
					commandProcessor.process(consoleSystem, command);			
				} catch (Exception e) {
					consoleSystem.printMessage("Can't process the command. Exception was thrown.");
					consoleSystem.printBlankLine();
					e.printStackTrace(consoleSystem.getOutputStream());
				}
				
				return true;
			}
		}
		
		if (ICommandProcessor.DEFAULT_COMMAND_GROUP.equals(group)) {
			consoleSystem.printMessageLine(String.format("Unknown command: '%s'", command));			
		} else {
			consoleSystem.printMessageLine(String.format("Unknown command: '%s:%s'", group, command));
		}
		
		return false;
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
	
	public void stop() {
		stop = true;
	}

	private void doPrintDefaultHelp() throws Exception {
		try {
			getDefaultCommandProcessor().process(consoleSystem, "help");
		} catch (Exception e) {
			// Why???. Ignore it.
		}
	}
	
	private void printPrompt() {
		consoleSystem.printMessage("$");		
	}
}
