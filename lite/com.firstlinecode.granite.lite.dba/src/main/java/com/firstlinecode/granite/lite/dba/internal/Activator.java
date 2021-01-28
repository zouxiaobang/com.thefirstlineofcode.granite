package com.firstlinecode.granite.lite.dba.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.firstlinecode.granite.framework.core.supports.data.IDataObjectFactory;

public class Activator implements BundleActivator {
	private ServiceRegistration<IDataObjectFactory> srDataObjectFactory;
	@Override
	public void start(BundleContext context) throws Exception {
		srDataObjectFactory = context.registerService(IDataObjectFactory.class,
				new DataObjectFactory(context), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		srDataObjectFactory.unregister();
	}

}
