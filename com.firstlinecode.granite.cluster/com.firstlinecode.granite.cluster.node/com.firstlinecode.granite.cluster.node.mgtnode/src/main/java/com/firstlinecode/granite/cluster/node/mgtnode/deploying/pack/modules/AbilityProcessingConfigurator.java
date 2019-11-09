package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.modules;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackConfigurator;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackContext;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.ConfigFiles;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.IConfig;

public class AbilityProcessingConfigurator implements IPackConfigurator {

	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		IConfig config = context.getConfigManager().createOrGetConfig(context.getRuntimeGraniteConfigDir(),
				ConfigFiles.GRANITE_COMPONENT_BINDING_CONFIG_FILE);
		configureProcessingService(config);
		configureRoutingService(config);
	}

	private void configureProcessingService(IConfig config) {
		config.addOrUpdateProperty("processing.service$parsing.message.receiver", "cluster.parsing.2.processing.message.receiver");
		config.addOrUpdateProperty("cluster.parsing.2.processing.message.receiver$runtime.configuration", "cluster.runtime.configuration");
		config.addOrUpdateProperty("cluster.parsing.2.processing.message.receiver$session.manager", "cluster.session.manager");
		config.addOrUpdateProperty("cluster.parsing.2.processing.message.receiver$message.channel", "cluster.any.2.routing.message.channel");
		config.addOrUpdateProperty("cluster.parsing.2.processing.message.receiver$message.processor", "default.protocol.processing.processor");
		config.addOrUpdateProperty("default.protocol.processing.processor$authenticator", "cluster.authenticator");
		config.addOrUpdateProperty("default.protocol.processing.processor$event.message.channel", "cluster.any.2.event.message.channel");
		config.addOrUpdateProperty("default.protocol.processing.processor$presence.processor", "default.presence.processor");
		config.addOrUpdateProperty("default.presence.processor$event.message.channel", "cluster.any.2.event.message.channel");
		config.addOrUpdateProperty("default.protocol.processing.processor$message.processor", "default.message.processor");
		config.addOrUpdateProperty("default.message.processor$event.message.channel", "cluster.any.2.event.message.channel");
		config.addOrUpdateProperty("default.protocol.processing.processor$iq.result.processor", "default.iq.result.processor");
		config.addOrUpdateProperty("default.iq.result.processor$event.message.channel", "cluster.any.2.event.message.channel");
	}

	private void configureRoutingService(IConfig config) {
		config.addOrUpdateProperty("routing.service$processing.message.receiver", "cluster.any.2.routing.message.receiver");
		config.addOrUpdateProperty("cluster.any.2.routing.message.receiver$session.manager", "cluster.session.manager");
		config.addOrUpdateProperty("cluster.any.2.routing.message.receiver$message.channel", "cluster.routing.2.stream.message.channel");
		config.addOrUpdateProperty("cluster.routing.2.stream.message.channel$runtime.configuration", "cluster.runtime.configuration");
		config.addOrUpdateProperty("cluster.routing.2.stream.message.channel$router", "cluster.router");
		config.addOrUpdateProperty("cluster.router$session.manager", "cluster.session.manager");
		config.addOrUpdateProperty("cluster.any.2.routing.message.receiver$message.processor", "default.routing.processor");
	}

}
