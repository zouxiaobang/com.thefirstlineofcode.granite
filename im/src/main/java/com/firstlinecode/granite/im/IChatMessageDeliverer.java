package com.firstlinecode.granite.im;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.pipeline.stages.event.IEventFirer;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;

public interface IChatMessageDeliverer {
	boolean isMessageDeliverable(IProcessingContext context, Stanza stanza);
	void deliver(IProcessingContext context, IEventFirer eventService, Message message);
}