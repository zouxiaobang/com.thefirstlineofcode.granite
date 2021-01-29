package com.firstlinecode.granite.cluster.integration;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stream.error.NotAuthorized;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.event.IEvent;
import com.firstlinecode.granite.framework.core.integration.AbstractMessageReceiver;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.session.ISession;

public abstract class LocalMessageIntegrator extends AbstractMessageReceiver implements IMessageIntegrator, IConfigurationAware,
		IBundleContextAware {
	private static final Logger logger = LoggerFactory.getLogger(LocalMessageIntegrator.class);
	
	protected BundleContext bundleContext;
	
	private ServiceRegistration<IMessageIntegrator> sr;
	
	private ArrayBlockingQueue<IMessage> messageQueue;
	private int messageQueueMaxSize;
	
	private ExecutorService executorService;
	private Thread messageReaderThread;
	
	private volatile boolean stop;
	
	@Override
	protected void doStart() throws Exception {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(org.osgi.framework.Constants.SERVICE_PID, getOsgiServicePid());
		sr = bundleContext.registerService(IMessageIntegrator.class, this, properties);
		
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
							logger.trace(String.format("Message reader thread interrupted. Message receiver: %s", getClass().getName()), e);
						}
					}
				}
			}
			
		}, String.format("%s Message Reader Thread", getOsgiServicePid()));
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
					logger.warn("Null session id. Osgi service pid: {}. Message: {}", getOsgiServicePid(), message.getPayload());
					return;
				}
			}
			
			IConnectionContext context = getConnectionContext(jid);
			if (context != null) {
				messageProcessor.process(context, message);
			} else {
				logger.warn("Can't get connection context. {}. JID: {}", getOsgiServicePid(), jid);
			}
		}
	}

	@Override
	protected void doStop() throws Exception {
		stop = true;
		messageReaderThread.join();
		messageQueue = null;
		executorService = null;
		
		try {
			if (bundleContext.getBundle().getState() == Bundle.ACTIVE)
				sr.unregister();
		} catch (IllegalStateException e) {
			// ignore
		}
		
		sr = null;
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		messageQueueMaxSize = configuration.getInteger(getMessageQueueMaxSizeConfigurationKey(), getDefaultMessageQueueMaxSize());
	}

	@Override
	public void put(IMessage message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			logger.trace("error[message receiver]", e);
		}
	}
	
	@Override
	public IConnectionContext getConnectionContext(JabberId sessionJid) {
		if (sessionJid == null)
			throw new RuntimeException("Null session jid.");
		
		ISession session = sessionManager.get(sessionJid);
		if (session == null)
			throw new ProtocolException(new NotAuthorized(String.format("Null session. JID: %s", sessionJid)));
		
		return doGetConnectionContext(session);
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	protected abstract int getDefaultMessageQueueMaxSize();
	protected abstract  String getMessageQueueMaxSizeConfigurationKey();
	protected abstract String getOsgiServicePid();
	protected abstract IConnectionContext doGetConnectionContext(ISession session);
}
