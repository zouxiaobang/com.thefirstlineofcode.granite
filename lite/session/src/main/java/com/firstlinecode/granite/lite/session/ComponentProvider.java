package com.firstlinecode.granite.lite.session;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.repository.IComponentProvider;

@Extension
public class ComponentProvider implements IComponentProvider {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {SessionManager.class};
	}

}
