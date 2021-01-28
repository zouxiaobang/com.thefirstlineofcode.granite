package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.core.JabberId;

public class RoomItem {
	private JabberId jid;
	private String name;
	
	public JabberId getJid() {
		return jid;
	}
	
	public void setJid(JabberId jid) {
		this.jid = jid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
