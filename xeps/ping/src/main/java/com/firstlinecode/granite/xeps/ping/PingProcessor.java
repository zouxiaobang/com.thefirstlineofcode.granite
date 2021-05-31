package com.firstlinecode.granite.xeps.ping;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.xeps.ping.Ping;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.pipes.processing.IProcessingContext;
import com.firstlinecode.granite.framework.core.pipes.processing.IXepProcessor;

public class PingProcessor implements IXepProcessor<Iq, Ping>, IConfigurationAware {
	private static final String CONFIG_KEY_DISABLED = "disabled";
	private boolean disabled;

	@Override
	public void process(IProcessingContext context, Iq iq, Ping ping) {
		if (disabled) {
			ServiceUnavailable error = StanzaError.create(iq, ServiceUnavailable.class);
			context.write(error);
		} else {
			Iq pong = new Iq(Iq.Type.RESULT);
			pong.setId(iq.getId());
			pong.setObject(new Ping());
			
			context.write(pong);
		}
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		disabled = configuration.getBoolean(CONFIG_KEY_DISABLED, false);
	}
}
