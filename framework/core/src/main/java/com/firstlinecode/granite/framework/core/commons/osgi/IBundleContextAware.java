package com.firstlinecode.granite.framework.core.commons.osgi;

import org.osgi.framework.BundleContext;

public interface IBundleContextAware {
	void setBundleContext(BundleContext bundleContext);
}
