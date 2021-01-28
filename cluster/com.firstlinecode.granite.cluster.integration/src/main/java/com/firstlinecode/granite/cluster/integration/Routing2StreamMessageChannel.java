package com.firstlinecode.granite.cluster.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.routing.IForward;
import com.firstlinecode.granite.framework.core.routing.IRouter;

@Component("cluster.routing.2.stream.message.channel")
public class Routing2StreamMessageChannel implements IMessageChannel {
	private static final Logger logger = LoggerFactory.getLogger(Routing2StreamMessageChannel.class);
	
	@Dependency("runtime.configuration")
	private RuntimeConfiguration runtimeConfiguration;
	
	@Dependency("router")
	private IRouter router;

	@Override
	public void send(IMessage message) {
		JabberId target = (JabberId)message.getHeaders().get(IMessage.KEY_MESSAGE_TARGET);
		if (target != null) {
			Object payload = message.getPayload();
			if (payload instanceof Stanza) {
				target = ((Stanza)payload).getTo();
			}
		}
		
		if (target == null) {
			logger.warn("Null message target. Message content: {}.", message.getPayload());
			return;
		}
		
		IForward[] forwards = router.get(target);
		if (forwards == null || forwards.length == 0) {
			logger.warn("Can't forward message. Message content: {}. Message Target: {}", message.getPayload(), target.toString());
			// TODO Process offline messages.
			return;
		}
		
		for (IForward forward : forwards) {
			forward.to(message);
		}
	}

}
