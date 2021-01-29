package com.firstlinecode.granite.lite.xeps.component;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.framework.core.session.ValueWrapper;
import com.firstlinecode.granite.lite.integration.AbstractConnectionContext.MessageOutConnectionContext;
import com.firstlinecode.granite.lite.integration.AbstractConnectionContext.ObjectOutConnectionContext;
import com.firstlinecode.granite.lite.integration.AbstractConnectionContext.ProcessingContext;
import com.firstlinecode.granite.lite.integration.AbstractConnectionContext.StringOutConnectionContext;

@Component(value="lite.component.stream.2.parsing.message.receiver",
alias={
	"lite.component.parsing.2.processing.message.receiver",
	"lite.component.processing.2.routing.message.receiver"
}
)
public class MessageReceiver extends com.firstlinecode.granite.lite.integration.MessageReceiver {
	@Override
	public IConnectionContext getConnectionContext(JabberId sessionJid) {
		ISession session = new Session(sessionJid);
		
		if ("lite.component.stream.2.parsing".equals(pipePosition)) {
			return new ObjectOutConnectionContext(session, messageChannel);
		} else if ("lite.component.parsing.2.processing".equals(pipePosition)) {
			return new ProcessingContext(session, messageChannel);
		} else if ("lite.component.processing.2.routing".equals(pipePosition)) {
			return new MessageOutConnectionContext(session, messageChannel);
		} else if ("lite.component.routing.2.stream".equals(pipePosition)) {
			return new StringOutConnectionContext(session, messageChannel);
		}
		
		throw new RuntimeException("unknown service id");
	}
	
	private class Session implements ISession {
		private JabberId sessionJid;
		
		public Session(JabberId sessionJid) {
			this.sessionJid = sessionJid;
		}

		@Override
		public <T> T setAttribute(Object key, T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getAttribute(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getAttribute(Object key, T defaultValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T removeAttribute(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object[] getAttributeKeys() {
			throw new UnsupportedOperationException();
		}

		@Override
		public JabberId getJid() {
			return sessionJid;
		}

		@Override
		public <T> T setAttribute(Object key, ValueWrapper<T> wrapper) {
			throw new UnsupportedOperationException();
		}
		
	}
}
