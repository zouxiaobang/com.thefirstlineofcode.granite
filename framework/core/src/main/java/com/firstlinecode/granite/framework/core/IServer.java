package com.firstlinecode.granite.framework.core;

public interface IServer {
	void start() throws Exception;
	void stop() throws Exception;
	
	IServerContext getServerContext();
}
