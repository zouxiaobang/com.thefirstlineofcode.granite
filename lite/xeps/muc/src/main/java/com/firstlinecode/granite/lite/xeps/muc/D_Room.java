package com.firstlinecode.granite.lite.xeps.muc;

import com.firstlinecode.granite.framework.core.adf.data.IIdProvider;
import com.firstlinecode.granite.xeps.muc.Room;

public class D_Room extends Room implements IIdProvider<String> {
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
