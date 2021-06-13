package com.firstlinecode.granite.framework.core.pipes.parsing;

import com.firstlinecode.granite.framework.core.pipes.IPipesExtender;

public interface IPipesPreprocessor extends IPipesExtender {
	String beforeParsing(String message);
	Object afterParsing(Object object);
}
