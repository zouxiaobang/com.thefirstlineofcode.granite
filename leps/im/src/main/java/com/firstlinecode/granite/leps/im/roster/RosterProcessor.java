package com.firstlinecode.granite.leps.im.roster;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.session.ISession;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;

public class RosterProcessor implements IXepProcessor<Iq, Roster> {
	
	@Dependency("roster.operator")
	private RosterOperator rosterOperator;

	@Override
	public void process(IProcessingContext context, Iq iq, Roster roster) {
		JabberId userJid = context.getAttribute(ISession.KEY_SESSION_JID);
		
		if (iq.getType() == Iq.Type.SET) {
			rosterOperator.rosterSet(context, userJid, roster);
			rosterOperator.reply(context, userJid, iq.getId());
		} else if (iq.getType() == Iq.Type.GET) {
			rosterOperator.rosterGet(context, userJid, iq.getId());
		} else {
			throw new ProtocolException(new BadRequest("Roster result not supported."));
		}
		
	}

}
