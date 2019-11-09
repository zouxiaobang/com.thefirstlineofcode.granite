package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.xeps.muc.xconference.XConference;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;

public class XConferenceProcessor implements IXepProcessor<Message, XConference> {
	@Dependency("muc.protocols.processor")
	private MucProtocolsProcessor delegate;

	@Override
	public void process(IProcessingContext context, Message message, XConference xConference) {
		delegate.process(context, message, xConference);
	}

}
