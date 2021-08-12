package com.firstlinecode.granite.lite.xeps.muc;

import com.firstlinecode.basalt.xeps.muc.PresenceBroadcast;
import com.firstlinecode.granite.framework.core.adf.data.IIdProvider;

public class D_PresenceBroadcast extends PresenceBroadcast implements IIdProvider<String> {
	private String id;
	private String roomConfigId;
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	public String getRoomConfigId() {
		return roomConfigId;
	}

	public void setRoomConfigId(String roomConfigId) {
		this.roomConfigId = roomConfigId;
	}
	
}
