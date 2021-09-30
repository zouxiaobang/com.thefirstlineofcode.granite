package com.thefirstlineofcode.granite.pipeline.stages.processing;

import com.thefirstlineofcode.granite.framework.core.IService;
import com.thefirstlineofcode.granite.framework.core.annotations.Component;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.IMessageReceiver;

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
