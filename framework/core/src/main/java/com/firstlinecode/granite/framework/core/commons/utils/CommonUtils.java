package com.firstlinecode.granite.framework.core.commons.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class CommonUtils {
    private static final char SEPARATOR_KEY_VALUE = '=';
	private static final String SEPARATOR_PROPERTIES = ";";

	public static boolean equalsEvenNull(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        } else {
            return obj1.equals(obj2);
        }
    }

    public static boolean equalsExceptNull(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return false;
        }

        return obj1.equals(obj2);
    }
    
    public static Map<String, String> parsePropertiesString(String string, String[] acceptedStrings) {
    	Map<String, String> properties = new HashMap<>();
    	
    	if (string == null || "".equals(string)) {
    		return properties;
    	}
    	
    	
    	StringTokenizer tokenizer = new StringTokenizer(string, SEPARATOR_PROPERTIES);
    	while (tokenizer.hasMoreTokens()) {
    		String property = tokenizer.nextToken();
    		int separatorIndex = property.indexOf(SEPARATOR_KEY_VALUE);
    		if (property.length() < 3 || separatorIndex == -1 || separatorIndex == 0 ||
    				separatorIndex == property.length() - 1) {
    			throw new RuntimeException(String.format("Illegal property format: %s.", property));
    		}
    		
    		String name = property.substring(0, separatorIndex).trim();
    		checkPropertyName(name, acceptedStrings);
    		String value = property.substring(separatorIndex + 1, property.length()).trim();
    		
    		properties.put(name, value);
    	}
    	
    	return properties;
    }

	private static void checkPropertyName(String name, String[] acceptedStrings) {
		if (acceptedStrings != null && acceptedStrings.length != 0) {
			boolean accepted = false;
			for (String acceptedString : acceptedStrings) {
				if (name.equals(acceptedString)) {
					accepted = true;
					break;
				}
			}
			
			if (!accepted) {
				throw new IllegalArgumentException(String.format("Unknown property %s.", name));
			}
		}
	}
	
	public static String getInternalServerErrorMessage(RuntimeException e) {
		StringWriter sw = new StringWriter();
		BufferedWriter bw = new BufferedWriter(sw);
		PrintWriter pw = new PrintWriter(bw);
		e.printStackTrace(pw);
		
		try {
			bw.flush();
		} catch (IOException fe) {
			// ???
		}
		
		return sw.toString();
	}
}
