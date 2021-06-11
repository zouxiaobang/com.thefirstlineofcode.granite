package com.firstlinecode.granite.lite.pipes;

import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.pipes.IMessage;
import com.firstlinecode.granite.framework.core.pipes.IMessageChannel;
import com.firstlinecode.granite.framework.core.repository.IComponentIdAware;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.repository.IRepositoryAware;

@Component(value="lite.stream.2.parsing.message.channel",
	alias={
			"lite.parsing.2.processing.message.channel",
			"lite.any.2.event.message.channel",
			"lite.any.2.routing.message.channel",
			"lite.routing.2.stream.message.channel"
		}
)
public class MessageChannel implements IMessageChannel, IConfigurationAware, IComponentIdAware, IRepositoryAware {
	private static final String KEY_MESSAGE_CHANNEL_INTEGRATOR = "message.channel.integrator";
	
	protected String integratorServicePid;
	protected IRepository repository;
	
	private volatile IMessageIntegrator integrator;
	
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
		
		integrator = (IMessageIntegrator)repository.get(integratorServicePid);
		if (integrator == null)
			throw new RuntimeException(String.format("Can't get service by pid: %s.", integratorServicePid));
		
		return integrator;
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		integratorServicePid = configuration.getString(KEY_MESSAGE_CHANNEL_INTEGRATOR);
	}
	
	@Override
	public void setComponentId(String componentId) {
		if (integratorServicePid == null) {
			integratorServicePid = componentId.substring(0, componentId.length() - 16) + ".integrator";
		}
	}

	@Override
	public void setRepository(IRepository repository) {
		this.repository = repository;
	}

}
