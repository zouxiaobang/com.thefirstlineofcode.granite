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
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.session.ISessionListener;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCADMIN = new IqProtocolChain(MucAdmin.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCOWNER = new IqProtocolChain(MucOwner.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_IQ_MUCOWNER_XDATA = new IqProtocolChain().next(MucOwner.PROTOCOL).next(XData.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_PRESENCE_MUC = new PresenceProtocolChain(Muc.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_MESSAGE_MUCUSER = new MessageProtocolChain(MucUser.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_PRESENCE_MUCUSER = new PresenceProtocolChain(MucUser.PROTOCOL);
	private static final ProtocolChain PROTOCOLCHAIN_MESSAE_XCONFERENCE = new MessageProtocolChain(XConference.PROTOCOL);
	
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
