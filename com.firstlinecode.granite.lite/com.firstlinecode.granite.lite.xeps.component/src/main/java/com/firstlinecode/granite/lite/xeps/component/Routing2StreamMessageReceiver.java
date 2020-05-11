package com.firstlinecode.granite.lite.xeps.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManager;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.stream.IDeliveryMessageReceiver;

@Component(value="lite.component.any.2.stream.message.receiver")
public class Routing2StreamMessageReceiver extends MessageReceiver implements IDeliveryMessageReceiver {
	private static final Logger logger = LoggerFactory.getLogger(Routing2StreamMessageReceiver.class);
	
	private IConnectionManager connectionManager;

	@Override
	public void setConnectionManager(IConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@Override
	public IConnectionContext getConnectionContext(JabberId sessionJid) {
		return connectionManager.getConnectionContext(sessionJid);
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
			JabberId jid = (JabberId)(message.getHeaders().get(IMessage.KEY_MESSAGE_TARGET));
			
			if (jid == null) {
				logger.warn("Null message target. Integrator: {}. Message: {}.", integratorServicePid, message.getPayload());
				return;
			}
			
			IConnectionContext context = getConnectionContext(jid);
			if (context != null) {
				messageProcessor.process(context, message);
			} else {
				// TODO process offline message
			}
		}
	}

}
