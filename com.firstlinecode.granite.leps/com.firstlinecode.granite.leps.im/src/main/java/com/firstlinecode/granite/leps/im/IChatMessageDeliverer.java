package com.firstlinecode.granite.leps.im;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.event.IEventService;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

public interface IChatMessageDeliverer {
	boolean isMessageDeliverable(IProcessingContext context, Stanza stanza);
	void deliver(IProcessingContext context, IEventService eventService, Message message);
}