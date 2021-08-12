package com.firstlinecode.granite.framework.core.pipeline.stages.routing;

import com.firstlinecode.granite.framework.core.pipeline.IMessage;
import com.firstlinecode.granite.framework.core.pipeline.stages.IPipelineExtender;

public interface IPipelinePostprocessor extends IPipelineExtender {
	IMessage beforeRouting(IMessage message);
}
