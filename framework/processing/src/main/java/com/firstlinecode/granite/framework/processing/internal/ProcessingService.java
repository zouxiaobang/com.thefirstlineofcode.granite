package com.firstlinecode.granite.framework.processing.internal;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipe.IMessageReceiver;

@Component("processing.service")
public class ProcessingService implements IService {
	
	@Dependency("parsing.message.receiver")
	private IMessageReceiver parsingMessageReceiver;
	
	@Override
	public void start() throws Exception {
		parsingMessageReceiver.start();
	}

	@Override
	public void stop() throws Exception {
		parsingMessageReceiver.stop();
	}

}
