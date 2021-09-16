package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.xeps.muc.Muc;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessor;

public class MucPresenceProcessor implements IXepProcessor<Presence, Muc>{
	@Dependency("muc.protocols.delegator")
	private MucProtocolsDelegator delegator;

	@Override
	public void process(IProcessingContext context, Presence presence, Muc muc) {
		delegator.process(context, presence, muc);
	}

}
