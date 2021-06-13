package com.firstlinecode.granite.framework.core.pipes.routing;

import com.firstlinecode.granite.framework.core.pipes.IMessage;
import com.firstlinecode.granite.framework.core.pipes.IPipesExtender;

public interface IPipesPostprocessor extends IPipesExtender {
	IMessage beforeRouting(IMessage message);
}
