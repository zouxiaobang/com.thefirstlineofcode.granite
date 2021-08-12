package com.firstlinecode.granite.xeps.muc;

import org.pf4j.Extension;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.xeps.muc.Muc;
import com.firstlinecode.basalt.xeps.muc.admin.MucAdmin;
import com.firstlinecode.basalt.xeps.muc.owner.MucOwner;
import com.firstlinecode.basalt.xeps.muc.user.MucUser;
import com.firstlinecode.basalt.xeps.muc.xconference.XConference;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;
import com.firstlinecode.granite.framework.core.session.ISessionListener;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return new IXepProcessorFactory<?, ?>[] {
			new SingletonXepProcessorFactory<>(ProtocolChain.first(Iq.PROTOCOL).next(MucAdmin.PROTOCOL),
					new MucAdminProcessor()),
			new SingletonXepProcessorFactory<>(ProtocolChain.first(Iq.PROTOCOL).next(MucOwner.PROTOCOL),
					new MucOwnerProcessor()),
			new SingletonXepProcessorFactory<>(ProtocolChain.first(Presence.PROTOCOL).next(Muc.PROTOCOL),
					new MucPresenceProcessor()),
			new SingletonXepProcessorFactory<>(ProtocolChain.first(Message.PROTOCOL).next(MucUser.PROTOCOL),
					new MucUserMessageProcessor()),
			new SingletonXepProcessorFactory<>(ProtocolChain.first(Presence.PROTOCOL).next(MucUser.PROTOCOL),
					new MucUserPresenceProcessor()),
			new SingletonXepProcessorFactory<>(ProtocolChain.first(Message.PROTOCOL).next(XConference.PROTOCOL),
					new XConferenceProcessor()),
		};
	}
	
	@Override
	public ISessionListener[] getSessionListeners() {
		return new ISessionListener[] {
			new SessionListener()
		};
	}
}
