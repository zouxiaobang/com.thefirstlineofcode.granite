package com.thefirstlineofcode.granite.xeps.disco;

import org.pf4j.ExtensionPoint;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xeps.disco.DiscoInfo;
import com.thefirstlineofcode.basalt.xeps.disco.DiscoItems;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;

public interface IDiscoProvider extends ExtensionPoint {
	DiscoInfo discoInfo(IProcessingContext context, Iq iq, JabberId jid, String node);
	DiscoItems discoItems(IProcessingContext context, Iq iq, JabberId jid, String node);
}
