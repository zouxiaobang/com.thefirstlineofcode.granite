package com.firstlinecode.granite.framework.im;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.commons.utils.OrderComparator;
import com.firstlinecode.granite.framework.core.pipes.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

@Component("default.presence.processor")
public class DefaultPresenceProcessor implements IPresenceProcessor, IInitializable, IApplicationComponentServiceAware {
	private IApplicationComponentService appComponentService;
	private volatile List<IPresenceProcessor> presenceProcessors;	
	
	public DefaultPresenceProcessor() {
		presenceProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		List<Class<? extends IPresenceProcessorFactory>> processorFactoryClasses = appComponentService.
				getExtensionClasses(IPresenceProcessorFactory.class);
		for (Class<? extends IPresenceProcessorFactory> processorFactoryClass : processorFactoryClasses) {
			IPresenceProcessorFactory processorFactory = appComponentService.createExtension(processorFactoryClass);
			presenceProcessors.add(processorFactory.createProcessor());
		}
		
		Collections.sort(presenceProcessors, new OrderComparator<>());
	}
	
	@Override
	public boolean process(IProcessingContext context, Presence presence) {
		for (IPresenceProcessor presenceProcessor : presenceProcessors) {
			if (presenceProcessor.process(context, presence))
				return true;
		}
		
		return false;
	}
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

}
