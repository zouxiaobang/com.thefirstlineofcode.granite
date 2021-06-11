package com.firstlinecode.granite.lite.pipes;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.repository.IComponentContributor;

@Extension
public class ComponentContributor implements IComponentContributor {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {
			LocalNodeIdProvider.class,
			MessageChannel.class,
			MessageReceiver.class,
			Router.class,
			Routing2StreamMessageReceiver.class
		};
	}

}
