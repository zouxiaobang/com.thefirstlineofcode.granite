package com.firstlinecode.granite.framework.core.pipeline.event;

import com.firstlinecode.basalt.protocol.core.JabberId;

public class ConnectionClosedEvent implements IEvent {
	private String id;
	private JabberId jid;
	private String streamId;
	
	public ConnectionClosedEvent(String id, JabberId jid, String streamId) {
		this.id = id;
		this.jid = jid;
		this.streamId = streamId;
	}
	
	public String getId() {
		return id;
	}
	
	public JabberId getJid() {
		return jid;
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setJid(JabberId jid) {
		this.jid = jid;
	}
	
	@Override
	public Object clone() {
		return new ConnectionClosedEvent(id, jid, streamId);
	}
	
}
