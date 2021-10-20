package com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing;

import com.thefirstlineofcode.granite.framework.core.pipeline.IMessage;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtender;

public interface IPipesPostprocessor extends IPipelineExtender {
	IMessage beforeRouting(IMessage message);
}
