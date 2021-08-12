package com.firstlinecode.granite.xeps.disco;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;

public interface IDiscoProcessor {
	void discoInfo(IProcessingContext context, Iq iq, JabberId jid, String node);
	void discoItems(IProcessingContext context, Iq iq, JabberId jid, String node);
}
