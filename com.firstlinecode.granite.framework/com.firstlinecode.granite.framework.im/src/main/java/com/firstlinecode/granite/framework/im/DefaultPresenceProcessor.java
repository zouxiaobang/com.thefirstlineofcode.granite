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

import com.firstlinecode.basalt.protocol.im.stanza.Presence;
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
import com.firstlinecode.granite.framework.processing.IPresenceProcessor;
import com.firstlinecode.granite.framework.processing.IProcessingContext;

@Component("default.presence.processor")
public class DefaultPresenceProcessor implements IPresenceProcessor, IBundleContextAware,
			IContributionTracker, IInitializable  {
	private static final String KEY_GRANITE_PRESENCE_PROCESSORS = "Granite-Presence-Processors";
	
	private BundleContext bundleContext;
	private Map<Bundle, List<IPresenceProcessor>> bundleAndPresenceProcessors;
	private volatile List<IPresenceProcessor> sortedPresenceProcessors;
	
	private IApplicationComponentService appComponentService;
	
	@Dependency("event.message.channel")
	private IMessageChannel eventMessageChannel;
	
	private IEventService eventService;
	
	public DefaultPresenceProcessor() {
		bundleAndPresenceProcessors = new HashMap<>();
		sortedPresenceProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
		
		eventService = new EventService(eventMessageChannel);
		
		OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_PRESENCE_PROCESSORS, this);
	}


	@Override
	public boolean process(IProcessingContext context, Presence presence) {
		for (IPresenceProcessor presenceProcessor : sortedPresenceProcessors) {
			if (presenceProcessor.process(context, presence))
				return true;
		}
		
		return false;
	}
	
	private List<IPresenceProcessor> sortPresenceProcessors(Collection<IPresenceProcessor> values) {
		List<IPresenceProcessor> presenceProcessorsList = new ArrayList<>(values);
		Collections.sort(presenceProcessorsList, new OrderComparator<>());
		
		return presenceProcessorsList;
	}

	@Override
	public void found(Bundle bundle, String contribution) throws Exception {
		StringTokenizer st = new StringTokenizer(contribution, ",");
		List<IPresenceProcessor> presenceProcessors = new ArrayList<>();
		
		while (st.hasMoreTokens()) {
			presenceProcessors.add(registerPresenceProcessor(bundle, st.nextToken()));
		}
		
		bundleAndPresenceProcessors.put(bundle, presenceProcessors);
		sortedPresenceProcessors = sortPresenceProcessors(getAllProcessors());
	}
	
	private IPresenceProcessor registerPresenceProcessor(Bundle bundle, String contribution) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class<?> clazz = bundle.loadClass(contribution);
		if (!IPresenceProcessor.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(String.format("%s must implement %.",
					contribution, IPresenceProcessor.class));
		}

		IPresenceProcessor presenceProcessor = (IPresenceProcessor)clazz.newInstance();
		appComponentService.inject(presenceProcessor, bundle.getBundleContext());
		
		if (presenceProcessor instanceof IEventServiceAware) {
			((IEventServiceAware)presenceProcessor).setEventService(eventService);
		}

		return presenceProcessor;
	}
	
	private Collection<IPresenceProcessor> getAllProcessors() {
		Collection<IPresenceProcessor> allProcessors = new ArrayList<>();
		
		for (List<IPresenceProcessor> processors : bundleAndPresenceProcessors.values()) {
			allProcessors.addAll(processors);
		}
		
		return allProcessors;
	}

	@Override
	public void lost(Bundle bundle, String contribution) throws Exception {
		bundleAndPresenceProcessors.remove(bundle);
		sortedPresenceProcessors = sortPresenceProcessors(getAllProcessors());
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

}
