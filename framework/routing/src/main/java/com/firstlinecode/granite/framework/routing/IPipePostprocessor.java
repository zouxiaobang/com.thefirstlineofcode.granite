package com.firstlinecode.granite.framework.routing;

import com.firstlinecode.granite.framework.core.integration.IMessage;

public interface IPipePostprocessor {
	IMessage beforeRouting(IMessage message);
}
