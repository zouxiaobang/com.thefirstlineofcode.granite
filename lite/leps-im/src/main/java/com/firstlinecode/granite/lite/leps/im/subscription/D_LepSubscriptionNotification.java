package com.firstlinecode.granite.lite.leps.im.subscription;

import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;
import com.firstlinecode.granite.leps.im.subscription.LepSubscriptionNotification;

public class D_LepSubscriptionNotification extends LepSubscriptionNotification
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
