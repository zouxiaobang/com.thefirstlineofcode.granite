package com.firstlinecode.granite.xeps.ibr;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.repository.IComponentContributor;

@Extension
public class ComponentContributor implements IComponentContributor {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {Registrar.class, IbrSupportedClientMessageProcessor.class};
	}

}
