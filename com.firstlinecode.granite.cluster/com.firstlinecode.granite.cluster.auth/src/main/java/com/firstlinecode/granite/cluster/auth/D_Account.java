package com.firstlinecode.granite.cluster.auth;

import com.firstlinecode.granite.framework.core.auth.Account;
import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;

public class D_Account extends Account implements IIdProvider<String> {
	private String id;

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

}
