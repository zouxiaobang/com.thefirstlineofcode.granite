package com.firstlinecode.granite.leps.im.traceable;

import com.firstlinecode.basalt.leps.im.message.traceable.MsgStatus;
import com.firstlinecode.basalt.leps.im.message.traceable.Trace;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;
import com.firstlinecode.granite.leps.im.IChatMessageDeliverer;

public class TraceProcessor implements IXepProcessor<Iq, Trace> {
	@Dependency("trace.store")
	private ITraceStore traceStore;
	
	@Dependency("traceable.message.store")
	private ITraceableMessageStore traceableMessageStore;
	
	@Dependency("chat.message.deliverer")
	private IChatMessageDeliverer deliverer;
	
	@Override
	public void process(IProcessingContext context, Iq iq, Trace trace) {
		if (Iq.Type.RESULT.equals(iq.getType())) {
			processTraceResult(context, iq, trace);
		} else if (Iq.Type.SET.equals(iq.getType())) {
			processTraceSet(context, iq, trace);
		} else {
			throw new ProtocolException(new BadRequest("'type' attribute of iq trace protocol must be 'set' or 'result'."));
		}
		
	}

	private void processTraceSet(IProcessingContext context, Iq iq, Trace trace) {
		if (trace.getMsgStatuses().size() == 0) {
			return;
		}
		
		if (!deliverer.isMessageDeliverable(context, iq)) {
			return;
		}
		
		checkTraceSetObject(trace);
		
		context.write(new Iq(Iq.Type.RESULT, iq.getId()));
		
		removeTraceableMessageAndforwardPeerMsgTrace(context, iq, trace);
	}

	private void checkTraceSetObject(Trace trace) {
		for (MsgStatus msgStatus : trace.getMsgStatuses()) {
			if (msgStatus.getId() == null ||
					msgStatus.getStatus() == null ||
						msgStatus.getStatus() == MsgStatus.Status.SERVER_REACHED ||
							msgStatus.getFrom() == null ||
								msgStatus.getStamp() == null) {
				throw new ProtocolException(new BadRequest("Invalid trace."));
			}
		}
	}

	private void processTraceResult(IProcessingContext context, Iq iq, Trace trace) {
		JabberId jid = context.getJid();
		
		if (trace.getMsgStatuses().size() == 0)
			return;
		
		checkTraceResultObject(trace);
		
		for (MsgStatus msgStatus : trace.getMsgStatuses()) {
			traceStore.remove(jid, msgStatus.getId());
		}
	}

	private void checkTraceResultObject(Trace trace) {
		for (MsgStatus msgStatus : trace.getMsgStatuses()) {
			if (msgStatus.getId() == null) {
				throw new ProtocolException(new BadRequest("Invalid trace."));
			}
		}
	}

	private void removeTraceableMessageAndforwardPeerMsgTrace(IProcessingContext context, Iq iq, Trace trace) {
		traceStore.save(iq.getTo(), trace.getMsgStatuses());
		
		for (MsgStatus msgStatus : trace.getMsgStatuses()) {
			if (MsgStatus.Status.PEER_REACHED.equals(msgStatus.getStatus())) {
				traceableMessageStore.remove(context.getJid(), msgStatus.getId());
				traceableMessageStore.remove(context.getJid().getBareId(), msgStatus.getId());
			}
		}
		
		Iq peerMsgStatusAck = new Iq(Iq.Type.SET, Stanza.generateId());
		peerMsgStatusAck.setTo(iq.getTo());
		peerMsgStatusAck.setObject(new Trace(trace.getMsgStatuses()));
		
		context.write(peerMsgStatusAck);
	}

}
