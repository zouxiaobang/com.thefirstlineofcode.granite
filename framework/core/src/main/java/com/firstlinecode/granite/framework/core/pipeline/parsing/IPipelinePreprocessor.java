package com.firstlinecode.granite.framework.core.pipeline.parsing;

import com.firstlinecode.granite.framework.core.pipeline.IPipelineExtender;

public interface IPipelinePreprocessor extends IPipelineExtender {
	String beforeParsing(String message);
	Object afterParsing(Object object);
}
