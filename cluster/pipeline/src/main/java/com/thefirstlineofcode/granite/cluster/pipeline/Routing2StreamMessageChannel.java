package com.thefirstlineofcode.granite.cluster.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.core.stanza.Stanza;
import com.thefirstlineofcode.granite.framework.core.annotations.Component;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.IMessage;
import com.thefirstlineofcode.granite.framework.core.pipeline.IMessageChannel;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IForward;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IRouter;

@Component("cluster.routing.2.stream.message.channel")
public class Routing2StreamMessageChannel implements IMessageChannel {
	private static final Logger logger = LoggerFactory.getLogger(Routing2StreamMessageChannel.class);
	
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
