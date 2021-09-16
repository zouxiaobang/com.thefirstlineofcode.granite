package com.firstlinecode.granite.xeps.muc;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.adf.IAppComponentsContributor;

@Extension
public class AppComponentContributor implements IAppComponentsContributor {

	@Override
	public Class<?>[] getAppComponentClasses() {
		return new Class<?>[] {
			MucProtocolsDelegator.class
		};
	}

}
