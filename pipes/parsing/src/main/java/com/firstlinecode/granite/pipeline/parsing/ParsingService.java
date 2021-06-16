package com.firstlinecode.granite.pipes.parsing;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipes.IMessageReceiver;

@Component("parsing.service")
public class ParsingService implements IService {
	
	@Dependency("stream.message.receiver")
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
