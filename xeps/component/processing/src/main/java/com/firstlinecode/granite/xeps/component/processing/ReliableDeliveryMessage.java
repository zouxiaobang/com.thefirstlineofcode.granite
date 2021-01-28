package com.firstlinecode.granite.xeps.component.processing;

import com.firstlinecode.basalt.protocol.core.JabberId;

public class ReliableDeliveryMessage {
	private JabberId from;
	private JabberId to;
	private String id;
	private String message;
	
	public ReliableDeliveryMessage() {}
	
	public ReliableDeliveryMessage(String id, JabberId from, JabberId to, String message) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.message = message;
	}
	
	public JabberId getFrom() {
		return from;
	}
	
	public void setFrom(JabberId from) {
		this.from = from;
	}
	
	public JabberId getTo() {
		return to;
	}
	
	public void setTo(JabberId to) {
		this.to = to;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
