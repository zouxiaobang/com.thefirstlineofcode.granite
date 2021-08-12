package com.firstlinecode.granite.framework.core.pipeline.stages.event;

public class ConnectionOpenedEvent implements IEvent {
	private String id;
	private String ip;
	private int port;
	
	public ConnectionOpenedEvent(String id, String ip, int port) {
		this.id = id;
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public Object clone() {
		return new ConnectionOpenedEvent(id, ip, port);
	}
}
