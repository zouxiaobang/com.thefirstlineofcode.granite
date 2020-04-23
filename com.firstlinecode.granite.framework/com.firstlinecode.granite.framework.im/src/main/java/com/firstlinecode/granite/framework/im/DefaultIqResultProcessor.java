package com.firstlinecode.granite.framework.im;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.IContributionTracker;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.commons.utils.OrderComparator;
import com.firstlinecode.granite.framework.core.event.EventProducer;
import com.firstlinecode.granite.framework.core.event.IEventProducer;
import com.firstlinecode.granite.framework.core.event.IEventProducerAware;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.supports.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

@Component("default.iq.result.processor")
public class DefaultIqResultProcessor implements IIqResultProcessor, IBundleContextAware,
	IContributionTracker, IInitializable {

	private static final String KEY_GRANITE_IQ_RESULT_PROCESSORS = "Granite-Iq-Result-Processors";
	
	private BundleContext bundleContext;
	private Map<Bundle, List<IIqResultProcessor>> bundleAndIqResultProcessors;
	private volatile List<IIqResultProcessor> sortedIqResultProcessors;
	
	private IApplicationComponentService appComponentService;
	
	@Dependency("event.message.channel")
	private IMessageChannel eventMessageChannel;
	
	private IEventProducer eventProducer;
	
	public DefaultIqResultProcessor() {
		bundleAndIqResultProcessors = new HashMap<>();
		sortedIqResultProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
		
		eventProducer = new EventProducer(eventMessageChannel);
		
		OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_IQ_RESULT_PROCESSORS, this);
	}


	@Override
	public boolean process(IProcessingContext context, Iq iq) {
		if (iq.getType() != Iq.Type.RESULT)
			throw new ProtocolException(new BadRequest("Neither XEP nor IQ result."));
		
		if (iq.getId() == null) {
			throw new ProtocolException(new BadRequest("Null ID."));
		}
		
		for (IIqResultProcessor iqResultProcessor : sortedIqResultProcessors) {
			if (iqResultProcessor.process(context, iq))
				return true;
		}
		
		return false;
	}
	
	private List<IIqResultProcessor> sortIqResultProcessors(Collection<IIqResultProcessor> values) {
		List<IIqResultProcessor> iqResultProcessorsList = new ArrayList<>(values);
		Collections.sort(iqResultProcessorsList, new OrderComparator<>());
		
		return iqResultProcessorsList;
	}

	@Override
	public void found(Bundle bundle, String contribution) throws Exception {
		StringTokenizer st = new StringTokenizer(contribution, ",");
		List<IIqResultProcessor> iqResultProcessors = new ArrayList<>();
		
		while (st.hasMoreTokens()) {
			iqResultProcessors.add(registerIqResultProcessor(bundle, st.nextToken()));
		}
		
		bundleAndIqResultProcessors.put(bundle, iqResultProcessors);
		sortedIqResultProcessors = sortIqResultProcessors(getAllProcessors());
	}
	
	private IIqResultProcessor registerIqResultProcessor(Bundle bundle, String contribution) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class<?> clazz = bundle.loadClass(contribution);
		if (!IIqResultProcessor.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(String.format("%s must implement %.",
					contribution, IIqResultProcessor.class));
		}

		IIqResultProcessor iqResultProcessor = (IIqResultProcessor)clazz.newInstance();
		appComponentService.inject(iqResultProcessor, bundle.getBundleContext());
		
		if (iqResultProcessor instanceof IEventProducerAware) {
			((IEventProducerAware)iqResultProcessor).setEventProducer(eventProducer);
		}

		return iqResultProcessor;
	}
	
	private Collection<IIqResultProcessor> getAllProcessors() {
		Collection<IIqResultProcessor> allProcessors = new ArrayList<>();
		
		for (List<IIqResultProcessor> processors : bundleAndIqResultProcessors.values()) {
			allProcessors.addAll(processors);
		}
		
		return allProcessors;
	}

	@Override
	public void lost(Bundle bundle, String contribution) throws Exception {
		bundleAndIqResultProcessors.remove(bundle);
		sortedIqResultProcessors = sortIqResultProcessors(getAllProcessors());
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

}
