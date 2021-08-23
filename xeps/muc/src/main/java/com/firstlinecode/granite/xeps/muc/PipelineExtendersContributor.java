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
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;
import com.firstlinecode.granite.framework.core.session.ISessionListener;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCADMIN = ProtocolChain.first(Iq.PROTOCOL).next(MucAdmin.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCOWNER = ProtocolChain.first(Iq.PROTOCOL).next(MucOwner.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCOWNER_XDATA = ProtocolChain.first(Iq.PROTOCOL).next(MucOwner.PROTOCOL).next(XData.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_PRESENCE_MUC = ProtocolChain.first(Presence.PROTOCOL).next(Muc.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_MESSAGE_MUCUSER = ProtocolChain.first(Message.PROTOCOL).next(MucUser.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_PRESENCE_MUCUSER = ProtocolChain.first(Presence.PROTOCOL).next(MucUser.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_MESSAE_XCONFERENCE = ProtocolChain.first(Message.PROTOCOL).next(XConference.PROTOCOL);
	
	@Override
	protected NamingConventionParsableProtocolObject[] getNamingConventionParsableProtocolObjects() {
		return new NamingConventionParsableProtocolObject[] {
				new NamingConventionParsableProtocolObject(
						PROTOCOLCHAIN_IQ_MUCADMIN,
						MucAdmin.class),
				new NamingConventionParsableProtocolObject(
						PROTOCOLCHAIN_IQ_MUCOWNER,
						MucOwner.class),
				new NamingConventionParsableProtocolObject(
						PROTOCOLCHAIN_IQ_MUCOWNER_XDATA,
						XData.class),
				new NamingConventionParsableProtocolObject(
						PROTOCOLCHAIN_PRESENCE_MUC,
						Muc.class),
				new NamingConventionParsableProtocolObject(
						PROTOCOLCHAIN_MESSAGE_MUCUSER,
						MucUser.class),
				new NamingConventionParsableProtocolObject(
						PROTOCOLCHAIN_PRESENCE_MUCUSER,
						MucUser.class),
				new NamingConventionParsableProtocolObject(
						PROTOCOLCHAIN_MESSAE_XCONFERENCE,
						XConference.class)
		};
	}
	
	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return new IXepProcessorFactory<?, ?>[] {
			new SingletonXepProcessorFactory<>(
					PROTOCOLCHAIN_IQ_MUCADMIN,
					new MucAdminProcessor()),
			new SingletonXepProcessorFactory<>(
					PROTOCOLCHAIN_IQ_MUCOWNER,
					new MucOwnerProcessor()),
			new SingletonXepProcessorFactory<>(
					PROTOCOLCHAIN_PRESENCE_MUC,
					new MucPresenceProcessor()),
			new SingletonXepProcessorFactory<>(
					PROTOCOLCHAIN_MESSAGE_MUCUSER,
					new MucUserMessageProcessor()),
			new SingletonXepProcessorFactory<>(
					PROTOCOLCHAIN_PRESENCE_MUCUSER,
					new MucUserPresenceProcessor()),
			new SingletonXepProcessorFactory<>(
					PROTOCOLCHAIN_MESSAE_XCONFERENCE,
					new XConferenceProcessor()),
		};
	}
	
	@Override
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return new Class<?>[] {
			Muc.class,
			MucUser.class,
			MucOwner.class,
			MucAdmin.class,
			XConference.class
		};
	}
	
	@Override
	public ISessionListener[] getSessionListeners() {
		return new ISessionListener[] {
			new SessionListener()
		};
	}
}
