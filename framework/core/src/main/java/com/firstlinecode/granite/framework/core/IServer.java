package com.firstlinecode.granite.framework.core;

public interface IServer {
	public static final String GRANITE_APP_COMPONENT_COLLECTOR = "granite.app.component.collector";
	
	void start() throws Exception;
	void stop() throws Exception;
	
	IServerContext getContext();
}
