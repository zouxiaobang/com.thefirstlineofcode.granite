package com.firstlinecode.granite.lite.session;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.repository.IComponentsContributor;

@Extension
public class ComponentsContributor implements IComponentsContributor {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {SessionManager.class};
	}

}
