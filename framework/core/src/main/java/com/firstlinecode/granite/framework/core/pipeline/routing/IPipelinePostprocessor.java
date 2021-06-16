package com.firstlinecode.granite.framework.core.pipeline.routing;

import com.firstlinecode.granite.framework.core.pipeline.IMessage;
import com.firstlinecode.granite.framework.core.pipeline.IPipelineExtender;

public interface IPipelinePostprocessor extends IPipelineExtender {
	IMessage beforeRouting(IMessage message);
}
