package com.thefirstlineofcode.granite.xeps.ping;

import com.thefirstlineofcode.basalt.protocol.core.stanza.Iq;
import com.thefirstlineofcode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.basalt.protocol.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xeps.ping.Ping;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;

public class PingProcessor implements IXepProcessor<Iq, Ping>, IConfigurationAware {
	private static final String CONFIG_KEY_DISABLED = "disabled";
	private boolean disabled;

	@Override
	public void process(IProcessingContext context, Iq iq, Ping ping) {
		if (disabled) {
			ServiceUnavailable error = StanzaError.create(iq, ServiceUnavailable.class);
			context.write(error);
		} else {
			Iq pong = new Iq(Iq.Type.RESULT, iq.getId());
			context.write(pong);
		}
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		disabled = configuration.getBoolean(CONFIG_KEY_DISABLED, false);
	}
}
