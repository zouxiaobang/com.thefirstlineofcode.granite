package com.firstlinecode.granite.leps.im.traceable;

import com.firstlinecode.basalt.leps.im.message.traceable.MsgStatus;
import com.firstlinecode.basalt.leps.im.message.traceable.Trace;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.datetime.DateTime;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.oxm.IOxmFactory;
import com.firstlinecode.basalt.protocol.oxm.OxmService;
import com.firstlinecode.basalt.protocol.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.protocol.oxm.translators.im.MessageTranslatorFactory;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.core.event.IEventService;
import com.firstlinecode.granite.framework.core.event.IEventServiceAware;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.framework.processing.IXepProcessor;
import com.firstlinecode.granite.leps.im.IChatMessageDeliverer;

public class TraceableMessageProcessor implements IEventServiceAware, IXepProcessor<Message, Trace>,
			IApplicationConfigurationAware {
	@Dependency("chat.message.deliverer")
	private IChatMessageDeliverer deliverer;
	
	@Dependency("traceable.message.store")
	private ITraceableMessageStore traceableMessageStore;
	
	@Dependency("trace.store")
	private ITraceStore traceStore;
	
	private JabberId domainJid;
	
	private IEventService eventService;
	
	private IOxmFactory oxmFactory = OxmService.createMinimumOxmFactory();
	
	public TraceableMessageProcessor() {
		oxmFactory.register(
				Message.class,
				new MessageTranslatorFactory()
		);
		oxmFactory.register(
				Trace.class,
				new NamingConventionTranslatorFactory<>(
						Trace.class
				)
		);
	}
	
	@Override
	public void process(IProcessingContext context, Message message, Trace trace) {
		if (message.getId() == null) {
			throw new ProtocolException(new BadRequest("ID of traceable message can't be null."));
		}		
		
		if (message.getBodies().size() == 0) {
			throw new ProtocolException(new BadRequest("Message content is null."));
		}
		
		if (trace.getMsgStatuses().size() != 0) {
			throw new ProtocolException(new BadRequest("Msg status size of message trace protocol must be 0."));
		} 
		
		if (!deliverer.isMessageDeliverable(context, message))
			return;
		
		
		if (message.getFrom() == null) {
			message.setFrom(context.getJid());
		}
		
		traceableMessageStore.save(message.getTo(), message.getId(), oxmFactory.translate(message));
		
		sendServerReachedTrace(context, message.getId());
		
		deliverer.deliver(context, eventService, message);
	}

	private void sendServerReachedTrace(IProcessingContext context, String messageId) {
		Iq serverReached = new Iq(Iq.Type.SET, Stanza.generateId(MsgTrace.TRACE_ID_PREFIX));
		
		Trace trace = new Trace();
		serverReached.setObject(trace);
		serverReached.setTo(context.getJid());
		
		MsgStatus msgStatus = new MsgStatus(messageId, MsgStatus.Status.SERVER_REACHED,
				domainJid, new DateTime());
		trace.getMsgStatuses().add(msgStatus);
		
		traceStore.save(context.getJid(), msgStatus);
		
		context.write(serverReached);
	}

	@Override
	public void setEventService(IEventService eventService) {
		this.eventService = eventService;
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		domainJid = JabberId.parse(appConfiguration.getDomainName());
	}

}
