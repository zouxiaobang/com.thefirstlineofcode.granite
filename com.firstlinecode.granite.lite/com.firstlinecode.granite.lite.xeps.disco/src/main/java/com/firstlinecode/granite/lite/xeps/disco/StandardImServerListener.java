package com.firstlinecode.granite.lite.xeps.disco;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

import com.firstlinecode.granite.framework.core.annotations.AppComponent;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;

@AppComponent("standard.im.server.listener")
public class StandardImServerListener implements SynchronousBundleListener, IBundleContextAware {
	private boolean standardImServer = false;
	private boolean standardStream = false;
	
	@Override
	public synchronized void bundleChanged(BundleEvent event) {
		if (isStandardImServerBundle(event.getBundle())) {
			if (event.getType() == BundleEvent.STARTED) {
				standardImServer = true;
			} else if (event.getType() == BundleEvent.STOPPED) {
				standardImServer = false;
			}
		} else if (isStandardStreamBundle(event.getBundle())) {
			if (event.getType() == BundleEvent.STARTED) {
				standardStream = true;
			} else if (event.getType() == BundleEvent.STOPPED) {
				standardStream = false;
			}
		}
	}

	private boolean isStandardImServerBundle(Bundle bundle) {
		return bundle.getSymbolicName().equals("com.firstlinecode.granite.im");
	}
	
	private boolean isStandardStreamBundle(Bundle bundle) {
		return bundle.getSymbolicName().equals("com.firstlinecode.granite.stream.standard");
	}

	@Override
	public synchronized void setBundleContext(BundleContext bundleContext) {
		for (Bundle bundle : bundleContext.getBundles()) {
			if (bundle.getState() != Bundle.ACTIVE)
				continue;
			
			if (isStandardImServerBundle(bundle)) {
				standardImServer = true;
				
				if (standardStream && standardImServer)
					break;
			} else if (isStandardStreamBundle(bundle)) {
				standardStream = true;
				
				if (standardStream && standardImServer)
					break;
			}
		}
		
		bundleContext.addBundleListener(this);
	}
	
	public boolean isIMServer() {
		return standardImServer;
	}
	
	public boolean isStandardStream() {
		return standardStream;
	}
}
