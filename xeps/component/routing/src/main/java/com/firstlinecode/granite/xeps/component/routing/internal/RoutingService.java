package com.firstlinecode.granite.xeps.component.routing.internal;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.integration.IMessageReceiver;

@Component("component.routing.service")
public class RoutingService implements IService {
	
	@Dependency("processing.message.receiver")
	private IMessageReceiver processingMessageReceiver;
	
	@Override
	public void start() throws Exception {
		if (processingMessageReceiver != null)
			processingMessageReceiver.start();
	}

	@Override
	public void stop() throws Exception {
		if (processingMessageReceiver != null)
			processingMessageReceiver.stop();
	}

}

