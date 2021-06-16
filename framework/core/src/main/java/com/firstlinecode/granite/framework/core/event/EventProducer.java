package com.firstlinecode.granite.framework.core.event;

import com.firstlinecode.granite.framework.core.pipeline.IMessageChannel;
import com.firstlinecode.granite.framework.core.pipeline.SimpleMessage;

public class EventProducer implements IEventFirer {
	private IMessageChannel eventMessageChannel;
	
	public EventProducer(IMessageChannel eventMessageChannel) {
		this.eventMessageChannel = eventMessageChannel;
	}

	@Override
	public void fire(IEvent event) {
		eventMessageChannel.send(new SimpleMessage(event));
	}

}
