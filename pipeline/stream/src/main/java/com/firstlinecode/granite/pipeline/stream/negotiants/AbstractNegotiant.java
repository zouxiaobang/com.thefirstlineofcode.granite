package com.firstlinecode.granite.pipeline.stream.negotiants;

import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.pipeline.IMessage;
import com.firstlinecode.granite.pipeline.stream.IStreamNegotiant;

public abstract class AbstractNegotiant implements IStreamNegotiant {
	

	protected IStreamNegotiant next;
	protected boolean done = false;

	@Override
	public void setNext(IStreamNegotiant next) {
		this.next = next;
	}

	@Override
	public boolean negotiate(IClientConnectionContext context, IMessage message) {
		if (done) {
			if (next != null) {
				return next.negotiate(context, message);
			}
			
			return true;
		}
		
		if (doNegotiate(context, message)) {
			done = true;
		}
		
		return done && next == null;
	}
	
	protected void closeStream(IClientConnectionContext context, IOxmFactory oxmFactory) {
		context.write(oxmFactory.translate(new Stream(true)));
		context.close();
	}
	
	public boolean isDone() {
		return done;
	}

	protected abstract boolean doNegotiate(IClientConnectionContext context, IMessage message);

}
