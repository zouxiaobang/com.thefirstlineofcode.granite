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

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.IContributionTracker;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.commons.utils.OrderComparator;
import com.firstlinecode.granite.framework.core.event.EventService;
import com.firstlinecode.granite.framework.core.event.IEventService;
import com.firstlinecode.granite.framework.core.event.IEventServiceAware;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.supports.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.processing.IMessageProcessor;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

@Component("default.message.processor")
public class DefaultMessageProcessor implements IMessageProcessor, IBundleContextAware,
			IContributionTracker, IInitializable {
	private static final String KEY_GRANITE_MESSAGE_PROCESSORS = "Granite-Message-Processors";
	
	private BundleContext bundleContext;
	private Map<Bundle, List<IMessageProcessor>> bundleAndmessageProcessors;
	private volatile List<IMessageProcessor> sortedMessageProcessors;
	
	private IApplicationComponentService appComponentService;
	
	@Dependency("event.message.channel")
	private IMessageChannel eventMessageChannel;
	
	private IEventService eventService;
	
	public DefaultMessageProcessor() {
		bundleAndmessageProcessors = new HashMap<>();
		sortedMessageProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
		
		eventService = new EventService(eventMessageChannel);
		
		OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_MESSAGE_PROCESSORS, this);
	}

	@Override
	public boolean process(IProcessingContext context, Message message) {
		for (IMessageProcessor messageProcessor : sortedMessageProcessors) {
			if (messageProcessor.process(context, message))
				return true;
		}
		
		return false;
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public void found(Bundle bundle, String contribution) throws Exception {
		StringTokenizer st = new StringTokenizer(contribution, ",");
		List<IMessageProcessor> messageProcessors = new ArrayList<>();
		
		while (st.hasMoreTokens()) {
			messageProcessors.add(registerMessageProcessor(bundle, st.nextToken()));
		}
		
		bundleAndmessageProcessors.put(bundle, messageProcessors);
		sortedMessageProcessors = sortMessageProcessors(getAllProcessors());
	}

	private Collection<IMessageProcessor> getAllProcessors() {
		Collection<IMessageProcessor> allProcessors = new ArrayList<>();
		
		for (List<IMessageProcessor> processors : bundleAndmessageProcessors.values()) {
			allProcessors.addAll(processors);
		}
		
		return allProcessors;
	}

	private IMessageProcessor registerMessageProcessor(Bundle bundle, String contribution) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class<?> clazz = bundle.loadClass(contribution);
		if (!IMessageProcessor.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(String.format("%s must implement %.",
				contribution, IMessageProcessor.class));
		}
		
		IMessageProcessor messageProcessor = (IMessageProcessor)clazz.newInstance();
		appComponentService.inject(messageProcessor, bundle.getBundleContext());
		
		if (messageProcessor instanceof IEventServiceAware) {
			((IEventServiceAware)messageProcessor).setEventService(eventService);
		}
		
		return messageProcessor;
	}

	private List<IMessageProcessor> sortMessageProcessors(Collection<IMessageProcessor> values) {
		List<IMessageProcessor> messageProcessorsList = new ArrayList<>(values);
		Collections.sort(messageProcessorsList, new OrderComparator<>());
		
		return messageProcessorsList;
	}

	@Override
	public void lost(Bundle bundle, String contribution) throws Exception {
		bundleAndmessageProcessors.remove(bundle);
		sortMessageProcessors(getAllProcessors());
	}

}
