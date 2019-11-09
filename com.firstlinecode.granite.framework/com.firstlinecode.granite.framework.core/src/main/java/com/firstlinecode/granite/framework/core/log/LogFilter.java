package com.firstlinecode.granite.framework.core.log;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

public class LogFilter extends TurboFilter {
	private final static String GRANITE_LIBRARIES_NAMESPACE = "com.firstlinecode.granite.";
	private final static String PROPERTY_NAME_LOG_ENABLE_THIRDPARTIES = "granite.log.enable.thirdparties";
	
	private static boolean enableThirdParties = false;
	
	private String[] applicationNamespaces;
	
	static {
		String pEnableThirdParties = System.getProperty(PROPERTY_NAME_LOG_ENABLE_THIRDPARTIES);
		
		if (Boolean.valueOf(pEnableThirdParties)) {
			enableThirdParties = true;
		}
	}
	
	public LogFilter(String[] applicationNamespaces) {
		this.applicationNamespaces = applicationNamespaces;
	}
	
	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level,
			String format, Object[] params, Throwable t) {
		if (enableThirdParties)
			return FilterReply.NEUTRAL;
		
		if (logger.getName().startsWith(GRANITE_LIBRARIES_NAMESPACE)) {
			return FilterReply.NEUTRAL;
		}
		
		if (applicationNamespaces != null && applicationNamespaces.length != 0) {
			for (String applicationNamespace : applicationNamespaces) {
				if (logger.getName().startsWith(applicationNamespace))
					return FilterReply.NEUTRAL;
			}
		}
		
		return FilterReply.DENY;
	}

}
