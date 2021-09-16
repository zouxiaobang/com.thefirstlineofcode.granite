package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.muc.admin.MucAdmin;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessor;

public class MucAdminProcessor implements IXepProcessor<Iq, MucAdmin> {
	@Dependency("muc.protocols.delegator")
	private MucProtocolsDelegator delegator;

	@Override
	public void process(IProcessingContext context, Iq iq, MucAdmin mucAdmin) {
		delegator.process(context, iq, mucAdmin);
	}

}
