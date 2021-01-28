package com.firstlinecode.granite.framework.core.supports;

import org.osgi.framework.BundleContext;

public interface IApplicationComponentService {
	void inject(Object object, BundleContext bundleContext);
}
