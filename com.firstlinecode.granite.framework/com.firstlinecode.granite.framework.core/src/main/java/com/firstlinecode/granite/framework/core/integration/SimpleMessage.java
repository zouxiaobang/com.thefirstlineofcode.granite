package com.firstlinecode.granite.framework.core.integration;

import java.util.Collections;
import java.util.Map;

public class SimpleMessage implements IMessage {
	protected Map<Object, Object> header;
	protected Object payload;
	
	public SimpleMessage() {
		this(null, null);
	}
	
	public SimpleMessage(Object payload) {
		this(null, payload);
	}
	
	public SimpleMessage(Map<Object, Object> header, Object payload) {
		this.header = header;
		this.payload = payload;
	}
	
	public void setHeader(Map<Object, Object> header) {
		this.header = header;
	}

	@Override
	public Map<Object, Object> getHeader() {
		if (header == null) {
			header = Collections.emptyMap();
		}
		
		return Collections.unmodifiableMap(header);
	}
	
	public void setPayload(Object payload) {
		this.payload = payload;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

}
