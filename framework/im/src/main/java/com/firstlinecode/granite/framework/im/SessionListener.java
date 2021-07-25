package com.firstlinecode.granite.framework.im;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.Conflict;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionContext;
import com.firstlinecode.granite.framework.core.connection.IConnectionManager;
import com.firstlinecode.granite.framework.core.connection.IConnectionManagerAware;
import com.firstlinecode.granite.framework.core.session.ISessionListener;

public class SessionListener implements ISessionListener, IConnectionManagerAware {
	private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);
	
	@Dependency("resources.register")
	private IResourcesRegister register;
	
	@Dependency("resources.service")
	private IResourcesService resourceService;
	
	private IConnectionManager connectionManager;
	
	private static final IOxmFactory oxmFactory = OxmService.createMinimumOxmFactory();
	
	private static final String MESSAGE_CONFLICT = oxmFactory.translate(new Conflict());
	private static final String MESSAGE_CLOSE_STREAM = oxmFactory.translate(new Stream(true));

	@Override
	public void sessionEstablished(IConnectionContext context, JabberId sessionJid) throws Exception {
		try {
			register.register(sessionJid);
		} catch (ResourceRegistrationException e) {
			logger.error("Can't register resource. JID is {}.", sessionJid);
			throw e;
		}
		
	}

	@Override
	public void sessionClosing(IConnectionContext context, JabberId sessionJid) throws Exception {}
	
	@Override
	public void sessionClosed(IConnectionContext context, JabberId sessionJid) throws Exception {
		register.unregister(sessionJid);
	}

	@Override
	public void sessionEstablishing(IConnectionContext context, JabberId sessionJid) throws Exception {
		if (resourceService.getResource(sessionJid) != null) {
			IClientConnectionContext clientContext = (IClientConnectionContext)connectionManager.
					getConnectionContext(sessionJid);
			
			if (clientContext != null) {
				clientContext.write(MESSAGE_CONFLICT);
				clientContext.write(MESSAGE_CLOSE_STREAM);
				
				clientContext.close(true);
			}
		}
	}

	@Override
	public void setConnectionManager(IConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

}
