package com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtender;

public interface IPipesPreprocessor extends IPipelineExtender {
	String beforeParsing(String message);
	Object afterParsing(Object object);
}
