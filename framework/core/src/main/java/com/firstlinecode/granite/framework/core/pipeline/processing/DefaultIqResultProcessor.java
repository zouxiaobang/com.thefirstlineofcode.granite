package com.firstlinecode.granite.framework.core.pipeline.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.granite.framework.core.utils.OrderComparator;

public class DefaultIqResultProcessor implements IIqResultProcessor {
	private List<IIqResultProcessor> iqResultProcessors;
	
	public DefaultIqResultProcessor() {
		iqResultProcessors = new ArrayList<>();
	}
	
	public void setIqResultProcessors(List<IIqResultProcessor> iqResultProcessors) {
		this.iqResultProcessors = iqResultProcessors;
		Collections.sort(this.iqResultProcessors, new OrderComparator<>());
	}
	
	public void addIqResultProcessor(IIqResultProcessor iqResultProcessor) {
		if (!iqResultProcessors.contains(iqResultProcessor)) {			
			iqResultProcessors.add(iqResultProcessor);
			Collections.sort(iqResultProcessors, new OrderComparator<>());
		}
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

}
