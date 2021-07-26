package com.firstlinecode.granite.lite.im;

import com.firstlinecode.granite.framework.core.adf.data.IIdProvider;

public class D_SubscriptionNotification extends com.firstlinecode.granite.framework.im.SubscriptionNotification
		implements IIdProvider<String> {
	private String id;
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}
	
}
