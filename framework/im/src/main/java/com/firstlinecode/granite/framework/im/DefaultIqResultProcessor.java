package com.firstlinecode.granite.framework.im;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.pipeline.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.core.pipeline.processing.IIqResultProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.utils.OrderComparator;

public class DefaultIqResultProcessor implements IIqResultProcessor, IInitializable,
			IApplicationComponentServiceAware {
	private IApplicationComponentService appComponentService;
	private List<IIqResultProcessor> iqResultProcessors;
	
	public DefaultIqResultProcessor() {
		iqResultProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		List<Class<? extends IIqResultProcessorFactory>> processorFactoryClasses = appComponentService.
				getExtensionClasses(IIqResultProcessorFactory.class);
		for (Class<? extends IIqResultProcessorFactory> processorFactoryClass : processorFactoryClasses) {
			IIqResultProcessorFactory processorFactory = appComponentService.createExtension(processorFactoryClass);
			iqResultProcessors.add(processorFactory.createProcessor());
		}
		
		Collections.sort(iqResultProcessors, new OrderComparator<>());
	}
	
	@Override
	public boolean processResult(IProcessingContext context, Iq iq) {
		if (iq.getType() != Iq.Type.RESULT)
			throw new ProtocolException(new BadRequest("Neither XEP nor IQ result."));
		
		if (iq.getId() == null) {
			throw new ProtocolException(new BadRequest("Null ID."));
		}
		
		for (IIqResultProcessor iqResultProcessor : iqResultProcessors) {
			if (iqResultProcessor.processResult(context, iq))
				return true;
		}
		
		return false;
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

}
