package com.thefirstlineofcode.granite.cluster.pipeline;

import org.pf4j.Extension;

import com.thefirstlineofcode.granite.framework.core.repository.IComponentsContributor;

@Extension
public class ComponentsContributor implements IComponentsContributor {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {
			Any2EventMessageChannel.class,
			Any2EventMessageReceiver.class,
			Any2RoutingMessageChannel.class,
			Any2RoutingMessageReceiver.class,
			Parsing2ProcessingMessageChannel.class,
			Parsing2ProcessingMessageReceiver.class,
			Routing2StreamMessageChannel.class,
			Routing2StreamMessageReceiver.class,
			Stream2ParsingMessageChannel.class,
			Stream2ParsingMessageReceiver.class,
			DeployClusterComponentsRegistrar.class
		};
	}

}
