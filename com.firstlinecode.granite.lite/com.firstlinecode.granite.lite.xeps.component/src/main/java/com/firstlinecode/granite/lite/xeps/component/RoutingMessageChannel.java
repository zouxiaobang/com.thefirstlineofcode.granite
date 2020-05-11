package com.firstlinecode.granite.lite.xeps.component;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.lite.integration.IMessageIntegrator;

@Component("lite.component.routing.2.stream.message.channel")
public class RoutingMessageChannel extends com.firstlinecode.granite.lite.integration.MessageChannel
		implements IApplicationConfigurationAware {
	private static final String PID_ROUTING_2_STREAM_INTEGRATOR = "lite.routing.2.stream.integrator";
	
	private IMessageIntegrator routing2StreamIntegrator;
	
	private String domainName;
	private String[] domainAliasNames;
	
	@Override
	public void send(IMessage message) {
		JabberId target = (JabberId)message.getHeaders().get(IMessage.KEY_MESSAGE_TARGET);
		if (isToClient(target)) {
			getRoutingToStreamIntegrator().put(message);
		} else {
			getComponentRoutingToStreamIntegrator().put(message);
		}
	}

	private boolean isToClient(JabberId to) {
		if (to.getName() == null)
			return false;
		
		if (domainName.equals(to.getDomain()))
			return true;
		
		for (String domainAliasName : domainAliasNames) {
			if (domainAliasName.equals(to.getDomain()))
				return true;
		}
		
		return false;
	}

	private IMessageIntegrator getComponentRoutingToStreamIntegrator() {
		return super.getIntegratorService();
	}
	
	private synchronized IMessageIntegrator getRoutingToStreamIntegrator() {
		if (routing2StreamIntegrator != null)
			return routing2StreamIntegrator;
		
		routing2StreamIntegrator = OsgiUtils.getServiceByPID(bundleContext, IMessageIntegrator.class, PID_ROUTING_2_STREAM_INTEGRATOR);
		if (routing2StreamIntegrator == null)
			throw new RuntimeException(String.format("Can't get service by pid: %s.", PID_ROUTING_2_STREAM_INTEGRATOR));
		
		return routing2StreamIntegrator;
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		domainName = appConfiguration.getDomainName();
		domainAliasNames = appConfiguration.getDomainAliasNames();
	}
}
