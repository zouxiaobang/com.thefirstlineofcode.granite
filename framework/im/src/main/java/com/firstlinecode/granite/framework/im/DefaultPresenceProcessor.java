package com.firstlinecode.granite.framework.im;

import com.firstlinecode.granite.framework.core.annotations.Component;

@Component("default.presence.processor")
public class DefaultPresenceProcessor/* implements IPresenceProcessor, IBundleContextAware,
			IContributionTracker, IInitializable  */{
	/*private static final String KEY_GRANITE_PRESENCE_PROCESSORS = "Granite-Presence-Processors";
	
	private BundleContext bundleContext;
	private Map<Bundle, List<IPresenceProcessor>> bundleToPresenceProcessors;
	private volatile List<IPresenceProcessor> sortedPresenceProcessors;
	
	private IApplicationComponentService appComponentService;
	
	@Dependency("event.message.channel")
	private IMessageChannel eventMessageChannel;
	
	private IEventProducer eventProducer;
	
	public DefaultPresenceProcessor() {
		bundleToPresenceProcessors = new HashMap<>();
		sortedPresenceProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
		
		eventProducer = new EventProducer(eventMessageChannel);
		
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
		
		bundleToPresenceProcessors.put(bundle, presenceProcessors);
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
		
		if (presenceProcessor instanceof IEventProducerAware) {
			((IEventProducerAware)presenceProcessor).setEventProducer(eventProducer);
		}

		return presenceProcessor;
	}
	
	private Collection<IPresenceProcessor> getAllProcessors() {
		Collection<IPresenceProcessor> allProcessors = new ArrayList<>();
		
		for (List<IPresenceProcessor> processors : bundleToPresenceProcessors.values()) {
			allProcessors.addAll(processors);
		}
		
		return allProcessors;
	}

	@Override
	public void lost(Bundle bundle, String contribution) throws Exception {
		bundleToPresenceProcessors.remove(bundle);
		sortedPresenceProcessors = sortPresenceProcessors(getAllProcessors());
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}*/

}
