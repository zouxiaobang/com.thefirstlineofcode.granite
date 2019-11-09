package com.firstlinecode.granite.cluster.dba.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;

public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		System.setProperty("java.util.logging.config.file", OsgiUtils.getGraniteConfigDir(context).getPath() + "/java_util_logging.ini");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {}

}
