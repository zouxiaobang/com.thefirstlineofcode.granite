package com.thefirstlineofcode.granite.xeps.muc;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.protocol.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.protocol.core.MessageProtocolChain;
import com.thefirstlineofcode.basalt.protocol.core.PresenceProtocolChain;
import com.thefirstlineofcode.basalt.protocol.core.ProtocolChain;
import com.thefirstlineofcode.basalt.xeps.muc.Muc;
import com.thefirstlineofcode.basalt.xeps.muc.admin.MucAdmin;
import com.thefirstlineofcode.basalt.xeps.muc.owner.MucOwner;
import com.thefirstlineofcode.basalt.xeps.muc.user.MucUser;
import com.thefirstlineofcode.basalt.xeps.muc.xconference.XConference;
import com.thefirstlineofcode.basalt.xeps.xdata.XData;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCADMIN = new IqProtocolChain(MucAdmin.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCOWNER = new IqProtocolChain(MucOwner.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCOWNER_XDATA = new IqProtocolChain().next(MucOwner.PROTOCOL).next(XData.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_PRESENCE_MUC = new PresenceProtocolChain(Muc.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_MESSAGE_MUCUSER = new MessageProtocolChain(MucUser.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_PRESENCE_MUCUSER = new PresenceProtocolChain(MucUser.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_MESSAE_XCONFERENCE = new MessageProtocolChain(XConference.PROTOCOL);
	
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.
			registerNamingConventionParser(PROTOCOLCHAIN_IQ_MUCADMIN, MucAdmin.class).
			registerNamingConventionParser(PROTOCOLCHAIN_IQ_MUCOWNER, MucOwner.class).
			registerNamingConventionParser(PROTOCOLCHAIN_IQ_MUCOWNER_XDATA, XData.class).
			registerNamingConventionParser(PROTOCOLCHAIN_PRESENCE_MUC, Muc.class).
			registerNamingConventionParser(PROTOCOLCHAIN_MESSAGE_MUCUSER, MucUser.class).
			registerNamingConventionParser(PROTOCOLCHAIN_PRESENCE_MUCUSER, MucUser.class).
			registerNamingConventionParser(PROTOCOLCHAIN_MESSAE_XCONFERENCE, XConference.class);
		
		configurator.
			registerSingletonXepProcessor(PROTOCOLCHAIN_IQ_MUCADMIN, new MucAdminProcessor()).
			registerSingletonXepProcessor(PROTOCOLCHAIN_IQ_MUCOWNER, new MucOwnerProcessor()).
			registerSingletonXepProcessor(PROTOCOLCHAIN_PRESENCE_MUC, new MucPresenceProcessor()).
			registerSingletonXepProcessor(PROTOCOLCHAIN_MESSAGE_MUCUSER, new MucUserMessageProcessor()).
			registerSingletonXepProcessor(PROTOCOLCHAIN_PRESENCE_MUCUSER, new MucUserPresenceProcessor()).
			registerSingletonXepProcessor(PROTOCOLCHAIN_MESSAE_XCONFERENCE, new XConferenceProcessor());
		
		configurator.
			registerNamingConventionTranslator(Muc.class).
			registerNamingConventionTranslator(MucUser.class).
			registerNamingConventionTranslator(MucOwner.class).
			registerNamingConventionTranslator(MucAdmin.class).
			registerNamingConventionTranslator(XConference.class);
		
		configurator.registerSessionListener(new SessionListener());
	}
}
