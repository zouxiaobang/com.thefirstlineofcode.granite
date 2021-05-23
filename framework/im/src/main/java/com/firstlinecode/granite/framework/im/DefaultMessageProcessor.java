package com.firstlinecode.granite.framework.im;

import com.firstlinecode.granite.framework.core.annotations.Component;

@Component("default.message.processor")
public class DefaultMessageProcessor/* implements IMessageProcessor, IBundleContextAware,
			IContributionTracker, IInitializable*/ {
	/*private static final String KEY_GRANITE_MESSAGE_PROCESSORS = "Granite-Message-Processors";
	
	private BundleContext bundleContext;
	private Map<Bundle, List<IMessageProcessor>> bundleTomessageProcessors;
	private volatile List<IMessageProcessor> sortedMessageProcessors;
	
	private IApplicationComponentService appComponentService;
	
	@Dependency("event.message.channel")
	private IMessageChannel eventMessageChannel;
	
	private IEventProducer eventProducer;
	
	public DefaultMessageProcessor() {
		bundleTomessageProcessors = new HashMap<>();
		sortedMessageProcessors = new ArrayList<>();
	}
	
	@Override
	public void init() {
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
		
		eventProducer = new EventProducer(eventMessageChannel);
		
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
		
		bundleTomessageProcessors.put(bundle, messageProcessors);
		sortedMessageProcessors = sortMessageProcessors(getAllProcessors());
	}

	private Collection<IMessageProcessor> getAllProcessors() {
		Collection<IMessageProcessor> allProcessors = new ArrayList<>();
		
		for (List<IMessageProcessor> processors : bundleTomessageProcessors.values()) {
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
		
		if (messageProcessor instanceof IEventProducerAware) {
			((IEventProducerAware)messageProcessor).setEventProducer(eventProducer);
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
		bundleTomessageProcessors.remove(bundle);
		sortMessageProcessors(getAllProcessors());
	}
*/
}
