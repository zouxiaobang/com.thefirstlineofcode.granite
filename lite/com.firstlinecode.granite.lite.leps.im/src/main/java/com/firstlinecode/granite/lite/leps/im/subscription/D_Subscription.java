package com.firstlinecode.granite.lite.leps.im.subscription;

import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;
import com.firstlinecode.granite.framework.im.Subscription;

public class D_Subscription extends Subscription implements IIdProvider<String> {
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
