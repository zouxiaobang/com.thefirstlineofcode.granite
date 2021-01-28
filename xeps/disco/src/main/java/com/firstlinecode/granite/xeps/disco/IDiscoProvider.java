package com.firstlinecode.granite.xeps.disco;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;

public interface IDiscoProvider {
	DiscoInfo discoInfo(IProcessingContext context, Iq iq, JabberId jid, String node);
	DiscoItems discoItems(IProcessingContext context, Iq iq, JabberId jid, String node);
}
