package com.firstlinecode.granite.framework.routing;

import com.firstlinecode.granite.framework.core.pipe.IMessage;

public interface IPipePostprocessor {
	IMessage beforeRouting(IMessage message);
}
