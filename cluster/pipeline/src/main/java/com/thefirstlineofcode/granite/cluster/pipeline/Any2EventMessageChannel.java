package com.thefirstlineofcode.granite.cluster.pipeline;

import com.thefirstlineofcode.granite.framework.core.annotations.Component;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.IMessage;
import com.thefirstlineofcode.granite.framework.core.pipeline.IMessageChannel;
import com.thefirstlineofcode.granite.framework.core.pipeline.IMessageConnector;

@Component("cluster.any.2.event.message.channel")
public class Any2EventMessageChannel implements IMessageChannel {
	@Dependency(Constants.COMPONENT_ID_ANY_2_EVENT_MESSAGE_CONNECTOR)
	private IMessageConnector connector;

	@Override
	public void send(IMessage message) {
		connector.put(message);
	}
}
