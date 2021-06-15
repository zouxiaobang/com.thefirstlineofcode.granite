package com.firstlinecode.granite.xeps.ibr;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.repository.IComponentsContributor;

@Extension
public class ComponentsContributor implements IComponentsContributor {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {Registrar.class, IbrSupportedClientMessageProcessor.class};
	}

}
