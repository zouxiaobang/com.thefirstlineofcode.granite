package com.firstlinecode.granite.xeps.msgoffline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.datetime.DateTime;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.im.MessageTranslatorFactory;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.event.IEventContext;
import com.firstlinecode.granite.framework.core.event.IEventListener;
import com.firstlinecode.granite.framework.im.IOfflineMessageStore;
import com.firstlinecode.granite.framework.im.OfflineMessageEvent;
import com.firstlinecode.basalt.xeps.delay.Delay;

public class OfflineMessageListener implements IEventListener<OfflineMessageEvent>,
			IConfigurationAware {
	private static final Logger logger = LoggerFactory.getLogger(OfflineMessageListener.class);
	
	private static final String CONFIGURATION_KEY_DISABLED = "disabled";
	
	private boolean disabled;
	
	@Dependency("offline.message.store")
	private IOfflineMessageStore offlineMessageStore;
	
	private IOxmFactory oxmFactory = OxmService.createMinimumOxmFactory();
	
	public OfflineMessageListener() {
		oxmFactory.register(
				Message.class,
				new MessageTranslatorFactory()
		);
		oxmFactory.register(
				Delay.class,
				new NamingConventionTranslatorFactory<>(
						Delay.class
				)
		);
	}
	
	@Override
	public void process(IEventContext context, OfflineMessageEvent event) {
		if (disabled)
			return;
		
		JabberId jid = event.getContact();
		Message message = event.getMessage();
		
		if (message.getId() == null) {
			message.setId(Stanza.generateId("om"));
		}
		
		message.setObject(new Delay(message.getFrom(), new DateTime()));
		
		if (message.getFrom() == null) {
			message.setFrom(event.getUser());
		}
		
		try {
			offlineMessageStore.save(jid, message.getId(), oxmFactory.translate(message));
		} catch (Exception e) {
			logger.error("Can't save offline message.", e);
		}
		
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		disabled = configuration.getBoolean(CONFIGURATION_KEY_DISABLED, false);
	}

}
