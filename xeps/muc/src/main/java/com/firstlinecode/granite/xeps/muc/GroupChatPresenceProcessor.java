package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.granite.framework.core.annotations.BeanDependency;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.firstlinecode.granite.framework.im.IPresenceProcessor;

public class GroupChatPresenceProcessor implements IPresenceProcessor {
	@Dependency("muc.protocols.processor")
	private MucProtocolsProcessor delegate;
	
	@BeanDependency
	private IRoomService roomService;
	
	@Override
	public boolean process(IProcessingContext context, Presence presence) {
		if (presence.getType() != null && presence.getType() != Presence.Type.UNAVAILABLE)
			return false;
		
		if (presence.getTo() == null) {
			return false;
		}
		
		JabberId roomJid = presence.getTo().getBareId();
		if (!roomService.exists(roomJid)) {
			return false;
		}
		
		delegate.process(context, presence);
		
		return true;
	}

}

