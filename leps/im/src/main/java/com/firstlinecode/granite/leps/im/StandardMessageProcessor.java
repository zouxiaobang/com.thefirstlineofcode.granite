package com.firstlinecode.granite.leps.im;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.event.IEventProducer;
import com.firstlinecode.granite.framework.core.event.IEventProducerAware;
import com.firstlinecode.granite.framework.processing.IMessageProcessor;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

public class StandardMessageProcessor implements IMessageProcessor, IEventProducerAware {
	@Dependency("chat.message.deliverer")
	private IChatMessageDeliverer deliverer;
	
	private IEventProducer eventProducer;

	@Override
	public boolean process(IProcessingContext context, Message message) {
		if (!deliverer.isMessageDeliverable(context, message))
			return false;
		
		deliverer.deliver(context, eventProducer, message);
		
		return true;
	}

	@Override
	public void setEventProducer(IEventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}
}
