package com.firstlinecode.granite.im;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.event.IEventProducer;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

public interface IChatMessageDeliverer {
	boolean isMessageDeliverable(IProcessingContext context, Stanza stanza);
	void deliver(IProcessingContext context, IEventProducer eventService, Message message);
}