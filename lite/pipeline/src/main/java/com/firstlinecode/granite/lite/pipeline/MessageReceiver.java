package com.firstlinecode.granite.lite.pipeline;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.pipeline.AbstractMessageReceiver;
import com.firstlinecode.granite.framework.core.pipeline.IMessage;
import com.firstlinecode.granite.framework.core.pipeline.stages.event.IEvent;
import com.firstlinecode.granite.framework.core.repository.IComponentIdAware;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.repository.IRepositoryAware;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.lite.pipeline.AbstractConnectionContext.MessageOutConnectionContext;
import com.firstlinecode.granite.lite.pipeline.AbstractConnectionContext.ObjectOutConnectionContext;
import com.firstlinecode.granite.lite.pipeline.AbstractConnectionContext.ProcessingContext;
import com.firstlinecode.granite.lite.pipeline.AbstractConnectionContext.StringOutConnectionContext;

@Component(value="lite.stream.2.parsing.message.receiver",
	alias={
		"lite.parsing.2.processing.message.receiver",
		"lite.any.2.event.message.receiver",
		"lite.any.2.routing.message.receiver"
	}
)
public class MessageReceiver extends AbstractMessageReceiver implements IMessageIntegrator, IConfigurationAware,
		IComponentIdAware, IServerConfigurationAware, IRepositoryAware {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
	
	private static final String CONFIGURATION_KEY_MESSAGE_QUEUE_MAX_SIZE = "message.queue.max.size";
	private static final int DEFAULT_MESSAGE_QUEUE_MAX_SIZE = 1024 * 64;
	
	protected String pipePosition;
	protected String integratorServicePid;
	
	protected IRepository repository;
	
	private ArrayBlockingQueue<IMessage> messageQueue;
	private int messageQueueMaxSize;
	
	private ExecutorService executorService;
	private Thread messageReaderThread;
	
	private volatile boolean stop;
	
	private String domain;
	
	@Override
	protected void doStart() throws Exception {
		repository.registerSingleton(integratorServicePid, this);
		
		stop = false;
		messageQueue = new ArrayBlockingQueue<>(messageQueueMaxSize);
		executorService = Executors.newCachedThreadPool();
		messageReaderThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!stop) {
					try {
						IMessage message = messageQueue.poll(50, TimeUnit.MILLISECONDS);
						if (message != null) {
							executorService.execute(getTask(message));
						}
					} catch (InterruptedException e) {
						if (logger.isTraceEnabled()) {
							logger.trace(String.format("Error[message receiver]. Pipe position: %s.", pipePosition), e);
						}
					}
				}
			}
			
		}, String.format("Granite Lite Message Receiver[%s]", pipePosition));
		messageReaderThread.start();
	}
	
	protected Runnable getTask(IMessage message) {
		return new WorkingThread(message);
	}
	
	private class WorkingThread implements Runnable {
		private IMessage message;
		
		public WorkingThread(IMessage message) {
			this.message = message;
		}
		
		public void run() {
			JabberId jid = (JabberId)(message.getHeaders().get(IMessage.KEY_SESSION_JID));
			
			if (!(message.getPayload() instanceof IEvent)) {
				if (jid == null) {
					logger.warn("Null session ID. Integrator: {}. Message: {}.", integratorServicePid, message.getPayload());
					return;
				}
			}
			
			IConnectionContext context = getConnectionContext(jid);
			if (context != null) {
				messageProcessor.process(context, message);
			} else {
				logger.warn("Can't get connection context. Pipe position: {}. JID: {}.", pipePosition, jid);
			}
		}
	}

	@Override
	protected void doStop() throws Exception {
		stop = true;
		messageReaderThread.join();
		messageQueue = null;
		executorService = null;
		
		repository.removeSingleton(integratorServicePid);
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		messageQueueMaxSize = configuration.getInteger(CONFIGURATION_KEY_MESSAGE_QUEUE_MAX_SIZE, DEFAULT_MESSAGE_QUEUE_MAX_SIZE);
	}
	
	@Override
	public void setComponentId(String componentId) {
		pipePosition = componentId.substring(0, componentId.length() - 17);
		
		integratorServicePid = pipePosition + ".integrator";
	}
	

	@Override
	public void put(IMessage message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			logger.trace("Error[message receiver].", e);
		}
	}
	
	@Override
	public IConnectionContext getConnectionContext(JabberId sessionJid) {
		ISession session = null;
		
		if (sessionJid != null) {
			session = sessionManager.get(sessionJid);
			if (session == null && !domain.equals(sessionJid.toString()))
				return null;
		}
		
		if (Constants.PIPE_POSITION_LITE_STREAM_2_PARSING.equals(pipePosition)) {
			return new ObjectOutConnectionContext(session, messageChannel);
		} else if (Constants.PIPE_POSITION_LITE_PARSING_2_PROCESSING.equals(pipePosition)) {
			return new ProcessingContext(session, messageChannel);
		} else if (Constants.PIPE_POSITION_LITE_ANY_2_ROUTING.equals(pipePosition)) {
			return new MessageOutConnectionContext(session, messageChannel);
		} else if (Constants.PIPE_POSITION_LITE_ANY_2_EVENT.equals(pipePosition)) {
			return new MessageOutConnectionContext(session, messageChannel);
		} else if (Constants.PIPE_POSITION_LITE_ROUTING_2_STREAM.equals(pipePosition)) {
			return new StringOutConnectionContext(session, messageChannel);
		}
		
		throw new RuntimeException("Unknown service ID.");
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domain = serverConfiguration.getDomainName();
	}

	@Override
	public void setRepository(IRepository repository) {
		this.repository = repository;
	}

}
