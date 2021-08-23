package com.firstlinecode.granite.xeps.muc;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.xeps.muc.RoomConfig;

public interface IRoomService {
	int getTotalNumberOfRooms();
	List<RoomItem> getRoomItems();
	boolean exists(JabberId roomJid);
	void createRoom(JabberId roomJid, JabberId creator, RoomConfig roomConfig) throws DuplicateRoomException;
	boolean isRoomLocked(JabberId roomJid);
	void unlockRoom(JabberId roomJid);
	void updateRoomConfig(Room room, RoomConfig newRoomConfig);
	IRoomSession getRoomSession(JabberId roomJid);
	void addToMemberList(JabberId roomJid, JabberId member);
	void updateNick(JabberId roomJid, JabberId member, String nick);
	JabberId getAffiliatedUserJidByNick(JabberId roomJid, String nick);
}
