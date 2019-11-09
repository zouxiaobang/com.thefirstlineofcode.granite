package com.firstlinecode.granite.lite.auth;

import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;

public class D_Account extends com.firstlinecode.granite.framework.core.auth.Account implements IIdProvider<String> {
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
