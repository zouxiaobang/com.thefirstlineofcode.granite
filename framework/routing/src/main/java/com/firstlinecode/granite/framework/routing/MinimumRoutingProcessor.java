package com.firstlinecode.granite.framework.routing;

public class MinimumRoutingProcessor /*implements IMessageProcessor, IBundleContextAware,
			IInitializable, IApplicationConfigurationAware*/ {
	/*private static final Logger logger = LoggerFactory.getLogger(MinimumRoutingProcessor.class);
	
	private static final String SEPARATOR_TRANSLATORS = ",";
	private static final String PROPERTY_NAME_TYPE = "type";
	private static final String PROPERTY_NAME_CLASS = "class";
	private static final String PROPERTY_NAME_TRANSLATOR_FACTORY = "translator-factory";
	private static final String PROPERTY_NAME_PROTOCOL = "protocol";
	private static final String TYPE_SIMPLE = "simple";
	private static final String TYPE_NAMING_CONVENTION = "naming-convention";
	private static final String TYPE_CUSTOM = "custom";
	private static final String SEPARATOR_OF_LOCALNAME_NAMESPACE = "|";
	private static final String VALUE_NULL = "null";
	
	protected ITranslatingFactory translatingFactory;
	protected BundleContext bundleContext;
	
	private Map<Bundle, List<Class<?>>> bundleToClasses;
	private Map<Bundle, List<IPipePostprocessor>> bundleToPostprocessors;
	private List<IPipePostprocessor> postprocessors;
	
	private String domain;
	
	private String translatorsContributionKey;
	private String postprocessorsContributionKey;
	
	private IApplicationComponentService appComponentService;
	
	public MinimumRoutingProcessor(String translatorsContributionKey, String postprocessorsContributionKey) {
		this.translatorsContributionKey = translatorsContributionKey;
		this.postprocessorsContributionKey = postprocessorsContributionKey;
		translatingFactory = OxmService.createTranslatingFactory();
		bundleToClasses = new HashMap<>();
		postprocessors = new CopyOnWriteArrayList<>();
	}
	
	@Override
	public void init() {
		registerPredefinedTranslators();
		trackContributedTranslators();
		trackContributedPostprocessors();
	}
	
	protected void registerPredefinedTranslators() {
		translatingFactory.register(Iq.class, new IqTranslatorFactory());
		translatingFactory.register(Stream.class, new StreamTranslatorFactory());
		translatingFactory.register(StreamError.class, new StreamErrorTranslatorFactory());
		translatingFactory.register(StanzaError.class, new StanzaErrorTranslatorFactory());
	}

	protected void trackContributedTranslators() {
		OsgiUtils.trackContribution(bundleContext, translatorsContributionKey, new TranslatorsTracker());
	}
	
	protected void trackContributedPostprocessors() {
		OsgiUtils.trackContribution(bundleContext, postprocessorsContributionKey, new PostprocessorTracker());
	}
	
	private class PostprocessorTracker implements IContributionTracker {

		@Override
		public void found(Bundle bundle, String contribution) throws Exception {
			StringTokenizer tokenizer = new StringTokenizer(contribution, SEPARATOR_TRANSLATORS);
			
			List<IPipePostprocessor> postprocessors = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				String sPostprocessorClass = tokenizer.nextToken();
				Class<?> postprocessorClass = bundle.loadClass(sPostprocessorClass);
				if (!IPipePostprocessor.class.isAssignableFrom(postprocessorClass)) {
					throw new IllegalArgumentException(String.format("Pipe postprocessor %s must implement %s.",
							postprocessorClass.getName(), IPipePostprocessor.class.getName()));
				}
				
				IPipePostprocessor postprocessor = (IPipePostprocessor)postprocessorClass.newInstance();
				
				appComponentService.inject(postprocessor, bundleContext);
				
				postprocessors.add(postprocessor);
			}
			
			bundleToPostprocessors.put(bundle, postprocessors);
			MinimumRoutingProcessor.this.postprocessors.addAll(postprocessors);
		}

		@Override
		public void lost(Bundle bundle, String contribution) throws Exception {
			List<IPipePostprocessor> postprocessors = bundleToPostprocessors.remove(bundle);
			MinimumRoutingProcessor.this.postprocessors.removeAll(postprocessors);
		}
		
	}
	
	private class TranslatorsTracker implements IContributionTracker {
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void found(Bundle bundle, String contribution) throws Exception {
			StringTokenizer tokenizer = new StringTokenizer(contribution, SEPARATOR_TRANSLATORS);
			
			List<Class<?>> classes = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				String translaotorString = tokenizer.nextToken();
				
				Map<String, String> properties = CommonUtils.parsePropertiesString(translaotorString,
						new String[] {
							PROPERTY_NAME_CLASS,
							PROPERTY_NAME_TYPE,
							PROPERTY_NAME_TRANSLATOR_FACTORY,
							PROPERTY_NAME_PROTOCOL
						}
				);
				
				String sType = properties.get(PROPERTY_NAME_TYPE);
				if (sType == null) {
					sType = TYPE_CUSTOM;
				}
				
				Class<?> clazz;
				ITranslatorFactory<?> translatorFactory;
				if (TYPE_CUSTOM.equals(sType)) {
					String sTranslatorFactory = properties.get(PROPERTY_NAME_TRANSLATOR_FACTORY);
					if (sTranslatorFactory == null)
						throw new IllegalArgumentException("Null translator factory[register translator].");
					
					Class<?> translatorFactoryClass = bundle.loadClass(sTranslatorFactory);
					
					if (!(ITranslatorFactory.class.isAssignableFrom(translatorFactoryClass)))
						throw new RuntimeException(String.format("%s must implement %s[register translator].",
								translatorFactoryClass, ITranslatorFactory.class));
						
					translatorFactory = (ITranslatorFactory<?>)translatorFactoryClass.newInstance();
					clazz = translatorFactory.getType();
				} else {
					String sClass = properties.get(PROPERTY_NAME_CLASS);
					if (sClass == null)
						throw new IllegalArgumentException("Null class[register translator].");
					
					clazz = bundle.loadClass(sClass);
					
					if (TYPE_NAMING_CONVENTION.equals(sType)) {
						translatorFactory = new NamingConventionTranslatorFactory(clazz);
					} else {
						String sProtocol = properties.get(PROPERTY_NAME_PROTOCOL);
						if (sProtocol == null)
							throw new IllegalArgumentException("Null protocol[register translator].");
						
						Protocol protocol = parseProtocol(sProtocol);
						
						if (TYPE_SIMPLE.equals(sType)) {
							translatorFactory = new SimpleObjectTranslatorFactory(clazz, protocol);
						} else {
							throw new RuntimeException(String.format("Unknown translator type: %s.", sType));
						}
					}
				}
				
				translatingFactory.register(clazz, translatorFactory);
				classes.add(clazz);
			}
			
			bundleToClasses.put(bundle, classes);
		}
		
		private Protocol parseProtocol(String sProtocol) {
			String localName;
			String namespace;
			
			int seperator = sProtocol.indexOf(SEPARATOR_OF_LOCALNAME_NAMESPACE);
			if (seperator == -1) {
				localName = sProtocol.trim();
				namespace = VALUE_NULL;
			} else {
				localName = sProtocol.substring(0, seperator).trim();
				namespace = sProtocol.substring(seperator + 1, sProtocol.length()).trim();
			}
			
			if (localName.length() == 0 || namespace.length() == 0) {
				throw new IllegalArgumentException(String.format("Invalid protocol.", sProtocol));
			}
			
			if (VALUE_NULL.equals(namespace)) {
				namespace = null;
			}
			
			return new Protocol(namespace, localName);
		}

		@Override
		public void lost(Bundle bundle, String contribution) throws Exception {
			List<Class<?>> classes = bundleToClasses.remove(bundle);
			
			if (classes != null) {
				for (Class<?> clazz : classes) {
					translatingFactory.unregister(clazz);
				}
			}
		}
	}
	
	@Override
	public void process(IConnectionContext context, IMessage message) {
		JabberId sessionJid = (JabberId)message.getHeaders().get(IMessage.KEY_SESSION_JID);
		
		try {
			Object payload = message.getPayload();
			
			JabberId target = (JabberId)message.getHeaders().get(IMessage.KEY_MESSAGE_TARGET);
			
			if (target == null && (payload instanceof Stanza)) {
				target = ((Stanza)payload).getTo();
			}
			
			if (target == null) {
				target = sessionJid;
			}
			
			if (domain.equals(target.toString())) {
				throw new RuntimeException("Try to route message to server itself. Maybe a application bug.");
			}
			
			routeToTarget(context, sessionJid, target, message.getPayload());
		} catch (Exception e) {
			routeException(context, sessionJid, e);
		}
	}

	private void routeToTarget(IConnectionContext context, JabberId sessionJid,
			JabberId target, Object out) {
		Map<Object, Object> headers = new HashMap<>();
		headers.put(IMessage.KEY_SESSION_JID, sessionJid);
		headers.put(IMessage.KEY_MESSAGE_TARGET, target);
		
		IMessage message = new SimpleMessage(headers, out);
		
		for (IPipePostprocessor postprocessor : postprocessors) {
			message = postprocessor.beforeRouting(message);
			
			if (message == null) {
				return;
			}
		}
		
		headers = message.getHeaders();
		if (headers.get(IMessage.KEY_SESSION_JID) == null) {
			headers.put(IMessage.KEY_SESSION_JID, sessionJid);
		}
		
		if (headers.get(IMessage.KEY_MESSAGE_TARGET) == null) {
			headers.put(IMessage.KEY_MESSAGE_TARGET, target);
		}
		
		out = message.getPayload();
		if ((out instanceof Stanza) && FlawedProtocolObject.isFlawed(out) &&
				!isFromServer((Stanza)out)) {
			// flawed object maybe misses some information.
			// so we try to use original message.
			String amendedOriginalMessage = getAmendedOriginalMessage((Stanza)out);
			if (amendedOriginalMessage != null)
				out = amendedOriginalMessage;
		}
		
		if (!(out instanceof String)) {
			out = translatingFactory.translate(out);
		}
		
		message = new SimpleMessage(headers, out);
		
		if (logger.isTraceEnabled()) {
			logger.trace("Routing message. Session ID: {}. Target: {}, Message: {}.",
					new Object[] {sessionJid, target, (String)message.getPayload()});
		}
		
		context.write(message);
	}

	private boolean isFromServer(Stanza stanza) {
		if (stanza.getFrom() == null)
			return true;
		
		return domain.equals(stanza.getFrom().toString());
	}
	
	// if original message doesn't set 'from' attribute(it implies stanza is from client send it).
	// we need amend the message to add 'from' attribute.
	private String getAmendedOriginalMessage(Stanza stanza) {
		if (stanza.getOriginalMessage() == null)
			return null;
		
		return getAmendedOriginalXmlMessage(stanza);
	}

	private String getAmendedOriginalXmlMessage(Stanza stanza) {
		String originalMessage = stanza.getOriginalMessage();
		String firstElementStartPart = getXmlMessageFirstElementStartPart(originalMessage);
		if (firstElementStartPart == null)
			return null;
		
		if (firstElementStartPart.indexOf(" from=") != -1)
			return null;
		
		int fromInsertPosition = firstElementStartPart.indexOf(' ');
		if (fromInsertPosition == -1) {
			fromInsertPosition = firstElementStartPart.indexOf('>');
		}
		
		String fromString = String.format(" from=\"%s\"", stanza.getFrom().toString());
		int firstElementStartPartLength = firstElementStartPart.length();
		String newFirstElementStartPart = firstElementStartPart.substring(0, fromInsertPosition)
				+ fromString + firstElementStartPart.substring(fromInsertPosition, firstElementStartPart.length());
		
		return newFirstElementStartPart + originalMessage.substring(firstElementStartPartLength, originalMessage.length());
	}

	private String getXmlMessageFirstElementStartPart(String originalMessage) {
		int elementStartEndIndex = originalMessage.indexOf('>');
		if (elementStartEndIndex == -1) {
			// ??? isn't it a xmpp stanza message?
			return null;
		}
		
		return originalMessage.substring(0, elementStartEndIndex + 1);
	}

	private void routeException(IConnectionContext context, JabberId sessionJid, Exception e) {
		String msgString;
		try {
			if (e instanceof ProtocolException) {
				msgString = translatingFactory.translate(((ProtocolException)e).getError());
			} else {
				msgString = translatingFactory.translate(new InternalServerError(e.getMessage()));
			}
			
			routeToTarget(context, sessionJid, sessionJid, msgString);
		} catch (Exception exception) {
			logger.error("routing error", exception);
		}
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		this.domain = appConfiguration.getDomainName();
	}*/

}
