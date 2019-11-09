package com.firstlinecode.granite.lite.xeps.component;

import com.firstlinecode.granite.framework.core.annotations.Component;

@Component(value="lite.component.stream.2.parsing.message.channel",
alias={
		"lite.component.parsing.2.processing.message.channel",
		"lite.component.processing.2.routing.message.channel"
	}
)
public class MessageChannel extends com.firstlinecode.granite.lite.integration.MessageChannel {
	@Override
	public void setComponentId(String componentId) {
		if (integratorServicePid == null) {
			integratorServicePid = componentId.substring(0, componentId.length() - 16) + ".integrator";
		}
	}
}
