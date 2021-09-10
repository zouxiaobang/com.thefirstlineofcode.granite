package com.firstlinecode.granite.framework.core.console;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import org.pf4j.Extension;

@Extension
public abstract class AbstractCommandsProcessor implements ICommandsProcessor {
	private static final String NAME_PREFIX_OF_PROCESS_METHOD = "process";

	@Override
	public boolean process(IConsoleSystem consoleSystem, String command, String... args) throws Exception {
		boolean matchMethodName = false;
		for (Method method : getClass().getDeclaredMethods()) {
			if (!isMethodNameMatched(method, command)) {
				continue;
			} else {
				matchMethodName = true;
			}
			
			if (!isParametersMatched(method, args)) {
				continue;
			}
			
			processCommand(consoleSystem, method, args);
			return true;
		}
		
		if (matchMethodName) {
			consoleSystem.printMessageLine(String.format("Illegal command format: '%s'. Arguments don't match.", getCommandFullName(command)));
			return false;
		} else {
			consoleSystem.printMessageLine(String.format("Unknown command: %s.", getCommandFullName(command)));
			return false;			
		}
	}
	
	protected String getCommandFullName(String commandSimpleName) {
		String group = getGroup();
		
		if (ICommandsProcessor.DEFAULT_COMMAND_GROUP.equals(group)) {
			return commandSimpleName;
		} else {
			return String.format("%s %s", group, commandSimpleName);
		}
	}

	private void processCommand(IConsoleSystem consoleSystem, Method method, String... args) throws Exception {
		if (Modifier.isPublic(method.getModifiers())) {
			invokeProcessMethod(consoleSystem, method, args);
		} else {
			Boolean oldAccessible = null;
			try {
				oldAccessible = method.isAccessible();
				method.setAccessible(true);
				invokeProcessMethod(consoleSystem, method, args);
			} catch (Exception e) {
				throw e;
			} finally {
				if (oldAccessible != null)
					method.setAccessible(oldAccessible);
			}
		}
	}

	private void invokeProcessMethod(IConsoleSystem consoleSystem, Method method, String... args)
			throws IllegalAccessException, InvocationTargetException {
		if (args.length == 0) {
			method.invoke(this, consoleSystem);			
		} else if (args.length == 1) {
			invokeOneArgumentCommandProcessMethod(consoleSystem, method, args);
		} else if (args.length == 2) {
			invokeTwoArgumentCommandProcessMethod(consoleSystem, method, args);			
		} else {
			method.invoke(this, consoleSystem, args);			
		}
	}

	private void invokeTwoArgumentCommandProcessMethod(IConsoleSystem consoleSystem, Method method, String... args)
			throws IllegalAccessException, InvocationTargetException {
		if (method.getParameters()[1].getType() == String.class &&
				method.getParameters()[2].getType() == String.class)
			method.invoke(this, consoleSystem, args[0], args[1]);
		else
			method.invoke(this, consoleSystem, args);
	}
	
	private void invokeOneArgumentCommandProcessMethod(IConsoleSystem consoleSystem, Method method, String... args)
			throws IllegalAccessException, InvocationTargetException {
		if (method.getParameters()[1].getType() == String.class)
			method.invoke(this, consoleSystem, args[0]);
		else
			method.invoke(this, consoleSystem, args);
	}

	private boolean isParametersMatched(Method method, String... args) {
		if (method.getParameterCount() == 0)
			return false;
		
		Parameter[] parameters = method.getParameters();
		if (parameters[0].getType() != IConsoleSystem.class)
			return false;
		
		if (args.length == 0) {
			return parameters.length == 1;
		} else if (args.length == 1) {
			return parameters.length == 2 && (parameters[1].getType() == String.class || parameters[1].getType() == String[].class);
		} else if (args.length == 2) {
			return parameters.length == 3 &&
					((parameters[1].getType() == String.class && parameters[2].getType() == String.class ) ||
					parameters[1].getType() == String[].class);
		} else {
			return parameters[1].getType() == String[].class;
		}
	}

	private boolean isMethodNameMatched(Method method, String command) {
		for (;;) {
			int slashIndex = command.indexOf("-");
			if (slashIndex == -1)
				break;
			
			command = command.substring(0, slashIndex) + Character.toUpperCase(command.charAt(slashIndex + 1)) +
					command.substring(slashIndex + 2, command.length());
		}
		
		String processCommandMethodName = NAME_PREFIX_OF_PROCESS_METHOD + Character.toUpperCase(command.charAt(0)) +
				command.substring(1, command.length());
		return method.getName().equals(processCommandMethodName);
	}
}
