package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.modules;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackConfigurator;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackContext;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.ConfigFiles;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.IConfig;

public class AbilityEventConfigurator implements IPackConfigurator {

	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		IConfig config = context.getConfigManager().createOrGetConfig(context.getRuntimeGraniteConfigDir(),
				ConfigFiles.GRANITE_COMPONENT_BINDING_CONFIG_FILE);
		configureEventService(config);
		configureRoutingService(config);
	}
	
	private void configureEventService(IConfig config) {
		config.addOrUpdateProperty("event.service$event.message.receiver", "cluster.any.2.event.message.receiver");
		config.addOrUpdateProperty("cluster.any.2.event.message.receiver$message.processor", "default.event.processor");
		config.addOrUpdateProperty("cluster.any.2.event.message.receiver$message.channel", "cluster.any.2.routing.message.channel");
		config.addOrUpdateProperty("cluster.any.2.event.message.receiver$session.manager", "cluster.session.manager");
	}

	private void configureRoutingService(IConfig config) {
		config.addPropertyIfAbsent("routing.service$processing.message.receiver", "cluster.any.2.routing.message.receiver");
		config.addPropertyIfAbsent("cluster.any.2.routing.message.receiver$session.manager", "cluster.session.manager");
		config.addPropertyIfAbsent("cluster.any.2.routing.message.receiver$message.channel", "cluster.routing.2.stream.message.channel");
		config.addPropertyIfAbsent("cluster.any.2.routing.message.receiver$message.processor", "default.routing.processor");
	}

}
