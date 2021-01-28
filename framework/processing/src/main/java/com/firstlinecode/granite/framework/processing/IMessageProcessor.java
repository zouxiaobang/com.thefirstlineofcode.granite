package com.firstlinecode.granite.framework.processing;

import com.firstlinecode.basalt.protocol.im.stanza.Message;

public interface IMessageProcessor {
	boolean process(IProcessingContext context, Message message);
}
