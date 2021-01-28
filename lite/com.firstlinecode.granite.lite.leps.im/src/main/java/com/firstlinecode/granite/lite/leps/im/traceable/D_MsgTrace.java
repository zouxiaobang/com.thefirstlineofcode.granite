package com.firstlinecode.granite.lite.leps.im.traceable;

import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;
import com.firstlinecode.granite.leps.im.traceable.MsgTrace;

public class D_MsgTrace extends MsgTrace implements IIdProvider<String> {
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
