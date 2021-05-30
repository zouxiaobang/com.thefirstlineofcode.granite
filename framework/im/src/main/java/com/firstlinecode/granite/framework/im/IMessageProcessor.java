package com.firstlinecode.granite.framework.im;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.pipes.processing.IProcessingContext;

public interface IMessageProcessor {
	boolean process(IProcessingContext context, Message message);
}
