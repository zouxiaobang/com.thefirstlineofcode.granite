package com.firstlinecode.granite.framework.core.pipeline.processing;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;

public interface IProcessingContext extends IConnectionContext {
	void write(JabberId target, Object message);
}
