package com.firstlinecode.granite.framework.core.event;

import com.firstlinecode.granite.framework.core.pipe.IMessageChannel;
import com.firstlinecode.granite.framework.core.pipe.SimpleMessage;

public class EventProducer implements IEventProducer {
	private IMessageChannel eventMessageChannel;
	
	public EventProducer(IMessageChannel eventMessageChannel) {
		this.eventMessageChannel = eventMessageChannel;
	}

	@Override
	public void fire(IEvent event) {
		eventMessageChannel.send(new SimpleMessage(event));
	}

}
