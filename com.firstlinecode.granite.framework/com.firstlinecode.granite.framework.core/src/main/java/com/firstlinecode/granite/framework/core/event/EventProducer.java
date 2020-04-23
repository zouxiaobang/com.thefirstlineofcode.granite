package com.firstlinecode.granite.framework.core.event;

import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.integration.SimpleMessage;

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
