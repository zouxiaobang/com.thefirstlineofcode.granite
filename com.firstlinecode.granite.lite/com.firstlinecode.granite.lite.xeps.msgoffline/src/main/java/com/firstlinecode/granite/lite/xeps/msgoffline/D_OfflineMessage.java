package com.firstlinecode.granite.lite.xeps.msgoffline;

import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;
import com.firstlinecode.granite.framework.im.OfflineMessage;

public class D_OfflineMessage extends OfflineMessage implements IIdProvider<String> {
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
