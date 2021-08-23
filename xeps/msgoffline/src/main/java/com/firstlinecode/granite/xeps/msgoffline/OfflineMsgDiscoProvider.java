package com.firstlinecode.granite.xeps.msgoffline;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.disco.Feature;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.firstlinecode.granite.xeps.disco.IDiscoProvider;

public class OfflineMsgDiscoProvider implements IDiscoProvider, IServerConfigurationAware, IConfigurationAware {
	private static final String CONFIGURATION_KEY_DISABLED = "disabled";
	
	private String domainName;
	private boolean disabled;
	
	private DiscoInfo discoInfo;
	
	public OfflineMsgDiscoProvider() {
		discoInfo = new DiscoInfo();
		discoInfo.getFeatures().add(new Feature("msgoffline"));
	}

	@Override
	public DiscoInfo discoInfo(IProcessingContext context, Iq iq, JabberId jid, String node) {
		if (disabled)
			return null;
		
		if (iq.getTo() == null || domainName.equals(iq.getTo().getDomain())) {
			return discoInfo;
		}
		
		return null;
	}

	@Override
	public DiscoItems discoItems(IProcessingContext context, Iq iq, JabberId jid, String node) {
		return null;
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		disabled = configuration.getBoolean(CONFIGURATION_KEY_DISABLED, false);
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}

}
