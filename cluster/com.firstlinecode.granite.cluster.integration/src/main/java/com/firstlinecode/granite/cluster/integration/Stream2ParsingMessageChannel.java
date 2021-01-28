package com.firstlinecode.granite.cluster.integration;

import org.osgi.framework.BundleContext;

import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;

@Component("cluster.stream.2.parsing.message.channel")
public class Stream2ParsingMessageChannel implements IMessageChannel, IBundleContextAware {
	private volatile IMessageIntegrator integrator;
	private BundleContext bundleContext;

	@Override
	public void send(IMessage message) {
		getIntegrator().put(message);
	}
	
	private IMessageIntegrator getIntegrator() {
		if (integrator != null)
			return integrator;
		
		return getIntegratorService();
	}
	
	protected synchronized IMessageIntegrator getIntegratorService() {
		if (integrator != null)
			return integrator;
		
		integrator = OsgiUtils.getServiceByPID(bundleContext, IMessageIntegrator.class, Constants.STREAM_2_PARSING_MESSAGE_INTEGRATOR);
		if (integrator == null)
			throw new RuntimeException("Can't get stream2parsing message integrator.");
		
		return integrator;
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

}
