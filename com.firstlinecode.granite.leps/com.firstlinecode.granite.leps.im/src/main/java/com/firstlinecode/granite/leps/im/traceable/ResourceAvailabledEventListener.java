package com.firstlinecode.granite.leps.im.traceable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.firstlinecode.basalt.leps.im.message.traceable.MsgStatus;
import com.firstlinecode.basalt.leps.im.message.traceable.Trace;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.datetime.DateTime;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.event.IEventContext;
import com.firstlinecode.granite.framework.core.event.IEventListener;
import com.firstlinecode.granite.framework.im.ResourceAvailabledEvent;

public class ResourceAvailabledEventListener implements IEventListener<ResourceAvailabledEvent>,
			IConfigurationAware {
	private static final String CONFIGURATION_KEY_MAX_TRACE_ITEM_SIZE = "max.trace.item.size";
	
	private int maxTraceItemSize;

	@Dependency("trace.store")
	private ITraceStore traceStore;
	
	@Dependency("traceable.message.store")
	private ITraceableMessageStore traceableMessageStore;
	
	@Override
	public void process(IEventContext context, ResourceAvailabledEvent event) {
		sendTraces(context, event);
		sendTraceableMessages(context, event);
	}

	private void sendTraceableMessages(IEventContext context, ResourceAvailabledEvent event) {
		JabberId resource = event.getJid();
		
		processTraceableMessages(context, resource, resource);
		processTraceableMessages(context, resource.getBareId(), resource);
	}
	
	private void processTraceableMessages(IEventContext context, JabberId target, JabberId resource) {
		List<String> removes = new ArrayList<>();
		Iterator<TraceableMessage> traceableMessages = traceableMessageStore.iterator(target);
		while (traceableMessages.hasNext()) {
			TraceableMessage traceableMessage = traceableMessages.next();
			removes.add(traceableMessage.getMessageId());
			
			context.write(resource, traceableMessage.getMessage());
		}
		
		for (String remove : removes) {
			traceableMessageStore.remove(target, remove);
		}
	}

	private void sendTraces(IEventContext context, ResourceAvailabledEvent event) {
		JabberId jid = event.getJid();
		if (traceStore.isEmpty(jid)) {
			return;
		}
		
		TraceIterator iterator = new TraceIterator(jid);
		
		while (iterator.hasNext()) {
			Trace trace = iterator.next();
			
			Iq iq = new Iq(Iq.Type.SET, Stanza.generateId(MsgTrace.TRACE_ID_PREFIX));
			iq.setTo(jid);
			iq.setObject(trace);
			
			context.write(iq);
		}
	}

	private class TraceIterator {
		private Iterator<MsgTrace> iterator;
		
		public TraceIterator(JabberId jid) {
			iterator = traceStore.iterator(jid);
		}
		
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		public Trace next() {
			if (!hasNext()) {
				return null;
			}
			
			Trace trace = new Trace();
			while (trace.getMsgStatuses().size() != maxTraceItemSize &&
					iterator.hasNext()) {
				MsgTrace msgTrace = iterator.next();
				MsgStatus msgStatus = new MsgStatus(msgTrace.getMessageId(), msgTrace.getStatus(),
						msgTrace.getFrom(), new DateTime(msgTrace.getStamp()));
				trace.getMsgStatuses().add(msgStatus);
			}
			
			return trace;
		}
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		maxTraceItemSize = configuration.getInteger(CONFIGURATION_KEY_MAX_TRACE_ITEM_SIZE, 20);
	}

}
