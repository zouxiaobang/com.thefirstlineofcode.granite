package com.firstlinecode.granite.framework.routing.internal;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.integration.IMessageReceiver;

@Component("routing.service")
public class RoutingService implements IService {
	@Dependency("processing.message.receiver")
	private IMessageReceiver processingMessageReceiver;
	
	@Dependency("event.message.receiver")
	private IMessageReceiver eventMessageReceiver;
	
	@Override
	public void start() throws Exception {
		if (eventMessageReceiver != null)
			eventMessageReceiver.start();
		
		if (processingMessageReceiver != null)
			processingMessageReceiver.start();
	}

	@Override
	public void stop() throws Exception {
		if (processingMessageReceiver != null)
			processingMessageReceiver.stop();
		
		if (eventMessageReceiver != null)
			eventMessageReceiver.stop();
	}

}
