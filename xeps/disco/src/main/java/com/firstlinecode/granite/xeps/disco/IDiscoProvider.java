package com.firstlinecode.granite.xeps.disco;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IProcessingContext;

public interface IDiscoProvider extends ExtensionPoint {
	DiscoInfo discoInfo(IProcessingContext context, Iq iq, JabberId jid, String node);
	DiscoItems discoItems(IProcessingContext context, Iq iq, JabberId jid, String node);
}
