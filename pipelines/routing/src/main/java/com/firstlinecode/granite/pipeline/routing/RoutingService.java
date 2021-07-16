package com.firstlinecode.granite.pipeline.routing;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.IMessageReceiver;

@Component("routing.service")
public class RoutingService implements IService {
	@Dependency("any.message.receiver")
	private IMessageReceiver anyMessageReceiver;
	
	@Override
	public void start() throws Exception {
		if (anyMessageReceiver != null)
			anyMessageReceiver.start();
	}

	@Override
	public void stop() throws Exception {
		if (anyMessageReceiver != null)
			anyMessageReceiver.stop();
	}
}
