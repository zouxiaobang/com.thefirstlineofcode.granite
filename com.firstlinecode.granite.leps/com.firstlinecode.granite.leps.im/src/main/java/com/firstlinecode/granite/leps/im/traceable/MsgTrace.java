package com.firstlinecode.granite.leps.im.traceable;

import java.util.Date;

import com.firstlinecode.basalt.leps.im.message.traceable.MsgStatus.Status;
import com.firstlinecode.basalt.protocol.core.JabberId;

public class MsgTrace {
	public static final String TRACE_ID_PREFIX = "trc";
	
	private JabberId jid;
	private String messageId;
	private Status status;
	private JabberId from;
	private Date stamp;
	
	public JabberId getJid() {
		return jid;
	}
	
	public void setJid(JabberId jid) {
		this.jid = jid;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public JabberId getFrom() {
		return from;
	}

	public void setFrom(JabberId from) {
		this.from = from;
	}

	public Date getStamp() {
		return stamp;
	}

	public void setStamp(Date stamp) {
		this.stamp = stamp;
	}
	
}
