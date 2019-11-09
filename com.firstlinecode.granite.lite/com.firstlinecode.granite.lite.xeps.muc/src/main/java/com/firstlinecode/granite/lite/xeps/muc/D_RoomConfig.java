package com.firstlinecode.granite.lite.xeps.muc;

import com.firstlinecode.granite.framework.core.supports.data.IIdProvider;

public class D_RoomConfig extends com.firstlinecode.basalt.xeps.muc.RoomConfig implements IIdProvider<String> {
	private String id;
	private String roomId;
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
}
