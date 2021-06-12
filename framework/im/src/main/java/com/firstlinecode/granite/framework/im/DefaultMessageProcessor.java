package com.firstlinecode.granite.framework.im;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.commons.utils.OrderComparator;
import com.firstlinecode.granite.framework.core.pipes.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

@Component("default.message.processor")
public class DefaultMessageProcessor implements IMessageProcessor, IInitializable, IApplicationComponentServiceAware {
	private IApplicationComponentService appComponentService;
	private List<IMessageProcessor> messageProcessors;	
	
	public DefaultMessageProcessor() {
		messageProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		List<Class<? extends IMessageProcessorFactory>> processorFactoryClasses = appComponentService.
				getExtensionClasses(IMessageProcessorFactory.class);
		for (Class<? extends IMessageProcessorFactory> processorFactoryClass : processorFactoryClasses) {
			IMessageProcessorFactory processorFactory = appComponentService.createExtension(processorFactoryClass);
			messageProcessors.add(processorFactory.createProcessor());
		}
		
		Collections.sort(messageProcessors, new OrderComparator<>());
	}

	@Override
	public boolean process(IProcessingContext context, Message message) {
		for (IMessageProcessor messageProcessor : messageProcessors) {
			if (messageProcessor.process(context, message))
				return true;
		}
		
		return false;
	}
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}
}
