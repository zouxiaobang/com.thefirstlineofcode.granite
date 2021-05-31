package com.firstlinecode.granite.xeps.ibr;

import com.firstlinecode.granite.framework.core.repository.IComponentProvider;

public class ComponentProvider implements IComponentProvider {

	@Override
	public Class<?>[] getComponentClasses() {
		return new Class<?>[] {IbrSupportedClientMessageProcessor.class};
	}

}
