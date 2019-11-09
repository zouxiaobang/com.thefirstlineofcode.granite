package com.firstlinecode.granite.lite.xeps.muc;

import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;
import com.firstlinecode.granite.xeps.muc.AffiliatedUser;

public class D_AffiliatedUser extends AffiliatedUser implements IIdProvider<String> {
	private String id;
	private String roomId;

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

}
