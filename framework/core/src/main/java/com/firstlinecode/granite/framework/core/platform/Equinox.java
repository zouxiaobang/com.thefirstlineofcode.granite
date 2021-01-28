package com.firstlinecode.granite.framework.core.platform;

import java.net.URL;

import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Equinox implements IPlatform {
	public BundleContext bundleContext;
	
	public Equinox(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public URL getConfigurationDirectory() {
		ServiceReference[] srLocations;
		try {
			srLocations = bundleContext.getServiceReferences(Location.class.getName(),
					"(type=osgi.configuration.area)");
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Invalid filter.");
		}
		
		Location location = null;
		if (srLocations != null && srLocations.length > 0) {
			location = (Location)bundleContext.getService(srLocations[0]);
		}
		
		if (location != null) {
			return location.getURL();
		}
		
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public URL getHomeDirectory() {
		ServiceReference[] srLocations;
		try {
			srLocations = bundleContext.getServiceReferences(
					Location.class.getName(), "(type=eclipse.home.location)");
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Invalid filter.");
		}
		
		Location location = null;
		if (srLocations != null && srLocations.length > 0) {
			location = (Location)bundleContext.getService(srLocations[0]);
		}
		
		if (location != null) {
			return location.getURL();
		}
		
		return null;
	}
}
