package com.firstlinecode.granite.xeps.muc;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.muc.owner.MucOwner;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;

public class MucOwnerProcessor implements IXepProcessor<Iq, MucOwner> {
	@Dependency("muc.protocols.processor")
	private MucProtocolsProcessor delegate;

	@Override
	public void process(IProcessingContext context, Iq iq, MucOwner mucOwner) {
		delegate.process(context, iq, mucOwner);
	}

}
