package com.firstlinecode.granite.framework.core.pipeline.event;

import com.firstlinecode.basalt.protocol.core.JabberId;

public class SessionEstablishedEvent implements IEvent {
	private String id;
	private JabberId jid;
	
	public SessionEstablishedEvent(String id, JabberId jid) {
		this.id = id;
		this.jid = jid;
	}
	
	public String getId() {
		return id;
	}
	
	public JabberId getJid() {
		return jid;
	}
	
	@Override
	public Object clone() {
		return new SessionEstablishedEvent(id, jid);
	}
}
