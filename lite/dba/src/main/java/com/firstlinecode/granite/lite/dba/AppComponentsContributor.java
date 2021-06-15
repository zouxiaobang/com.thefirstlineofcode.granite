package com.firstlinecode.granite.lite.dba;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.core.adf.IAppComponentsContributor;

@Extension
public class AppComponentsContributor implements IAppComponentsContributor {

	@Override
	public Class<?>[] getAppComponentClasses() {
		return new Class<?>[] {
			DataObjectFactory.class
		};
	}

}
