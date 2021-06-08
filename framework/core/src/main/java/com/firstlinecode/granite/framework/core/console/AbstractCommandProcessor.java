package com.firstlinecode.granite.framework.core.console;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class AbstractCommandProcessor implements ICommandProcessor {

	@Override
	public void process(IConsoleSystem consoleSystem, String command) throws Exception {
		for (Method method : getClass().getDeclaredMethods()) {
			if (isProcessCommandMethod(method, command)) {
				processCommand(method, consoleSystem);
				return;
			}
		}
	}

	private void processCommand(Method method, IConsoleSystem consoleSystem) throws Exception {
		if (Modifier.isPublic(method.getModifiers())) {
			method.invoke(this, consoleSystem);
		} else {
			Boolean oldAccessible = null;
			try {
				oldAccessible = method.isAccessible();
				method.setAccessible(true);
				method.invoke(this, consoleSystem);
			} catch (Exception e) {
				throw e;
			} finally {
				if (oldAccessible != null)
					method.setAccessible(oldAccessible);
			}
		}
	}

	private boolean isProcessCommandMethod(Method method, String command) {
		String processCommandMethodName = "process" + Character.toUpperCase(command.charAt(0)) +
				command.substring(1, command.length());
		if (!method.getName().equals(processCommandMethodName))
			return false;
		
		if (method.getParameterCount() != 1)
			return false;
		
		return method.getParameters()[0].getType() == IConsoleSystem.class;
	}
}
