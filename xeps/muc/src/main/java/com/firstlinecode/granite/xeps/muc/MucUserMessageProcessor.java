package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.xeps.muc.user.MucUser;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessor;

public class MucUserMessageProcessor implements IXepProcessor<Message, MucUser> {
	@Dependency("muc.protocols.delegator")
	private MucProtocolsDelegator delegator;

	@Override
	public void process(IProcessingContext context, Message message, MucUser mucUser) {
		delegator.process(context, message, mucUser);
	}

}
