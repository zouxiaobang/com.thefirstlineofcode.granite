package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.xeps.muc.user.MucUser;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessor;

public class MucUserPresenceProcessor implements IXepProcessor<Presence, MucUser> {
	@Dependency("muc.protocols.delegator")
	private MucProtocolsDelegator delegator;

	@Override
	public void process(IProcessingContext context, Presence presence, MucUser mucUser) {
		delegator.process(context, presence, mucUser);
	}

}
