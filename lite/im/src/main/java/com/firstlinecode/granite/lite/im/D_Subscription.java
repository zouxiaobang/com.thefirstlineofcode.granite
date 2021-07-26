package com.firstlinecode.granite.lite.im;

import com.firstlinecode.granite.framework.core.adf.data.IIdProvider;

public class D_Subscription extends com.firstlinecode.granite.framework.im.Subscription implements IIdProvider<String> {
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
