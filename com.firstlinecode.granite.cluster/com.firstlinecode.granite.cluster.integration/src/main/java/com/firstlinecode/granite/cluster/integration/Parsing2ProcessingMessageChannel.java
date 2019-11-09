package com.firstlinecode.granite.cluster.integration;

import org.osgi.framework.BundleContext;

import com.firstlinecode.granite.cluster.node.commons.deploying.NodeType;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.core.integration.IMessageChannel;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

@Component("cluster.parsing.2.processing.message.channel")
public class Parsing2ProcessingMessageChannel implements IMessageChannel, IBundleContextAware, IInitializable {
	private BundleContext bundleContext;
	private volatile IMessageIntegrator integrator;
	private boolean hasProcessingAbility;
	
	@Dependency("runtime.configuration")
	private RuntimeConfiguration runtimeConfiguration;

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

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
		
		integrator = OsgiUtils.getServiceByPID(bundleContext, IMessageIntegrator.class, Constants.PARSING_2_PROCESSING_MESSAGE_INTEGRATOR);
		if (integrator == null)
			throw new RuntimeException("Can't get stream2parsing message integrator.");
		
		return integrator;
	}

	@Override
	public void init() {
		NodeType nodeType = runtimeConfiguration.getDeployConfiguration().getNodeTypes().get(runtimeConfiguration.getNodeType());
		hasProcessingAbility = nodeType.hasAbility("processing");
		
		if (!hasProcessingAbility) {
			throw new RuntimeException("Splitting application vertically isn't supported yet. Appnode must possess all im abilities(stream, processing, event).");
		}
	}

}
