package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.granite.framework.core.adf.IAppComponentsContributor;

public class AppComponentContributor implements IAppComponentsContributor {

	@Override
	public Class<?>[] getAppComponentClasses() {
		return new Class<?>[] {
			MucProtocolsProcessor.class
		};
	}

}
