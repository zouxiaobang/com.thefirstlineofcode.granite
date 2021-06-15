package com.firstlinecode.granite.lite.xeps.ibr;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.repository.IComponentsContributor;

@Extension
public class ComponentsContributor implements IComponentsContributor {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {RegistrationStrategy.class};
	}

}
