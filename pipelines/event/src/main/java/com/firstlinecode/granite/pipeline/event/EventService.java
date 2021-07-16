package com.firstlinecode.granite.pipeline.event;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.IMessageReceiver;

@Component("event.service")
public class EventService implements IService {
	@Dependency("event.message.receiver")
	private IMessageReceiver eventMessageReceiver;
	
	@Override
	public void start() throws Exception {
		eventMessageReceiver.start();
	}

	@Override
	public void stop() throws Exception {
		eventMessageReceiver.stop();
	}

}
