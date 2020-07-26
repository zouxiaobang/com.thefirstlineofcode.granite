package com.firstlinecode.granite.lite.leps.im;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IApplicationConfigurationAware;
import com.firstlinecode.granite.framework.im.IResource;
import com.firstlinecode.granite.framework.im.IResourcesService;
import com.firstlinecode.granite.framework.im.ISubscriptionService;
import com.firstlinecode.granite.framework.im.Subscription;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.xeps.disco.IDiscoProvider;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.disco.Identity;
import com.firstlinecode.basalt.xeps.disco.Item;

public class ImClientDiscoProvider implements IDiscoProvider, IApplicationConfigurationAware {
	@Dependency("lep.im.server.listener")
	private LepImServerListener imServerListener;
	
	@Dependency("lite.authenticator")
	private IAuthenticator authenticator;
	
	@Dependency("subscription.service")
	private ISubscriptionService subscriptionService;
	
	@Dependency("resources.service")
	private IResourcesService resourceService;
	
	private String domain;
	
	
	@Override
	public DiscoInfo discoInfo(IProcessingContext context, Iq iq, JabberId jid, String node) {
		if (!imServerListener.isIMServer())
			return null;
		
		if (jid.isBareId() && domain.equals(jid.getDomain()) && node == null) {
			return discoAccountInfo(context, iq, jid);
		}
		
		return null;
	}

	private DiscoInfo discoAccountInfo(IProcessingContext context, Iq iq, JabberId jid) {
		if (!authenticator.exists(jid.getNode()))
			return null;
		
		Subscription subscription = subscriptionService.get(context.getJid().getNode(), jid.getBareIdString());
		if (subscription == null || (subscription.getState() != Subscription.State.FROM &&
				subscription.getState() != Subscription.State.BOTH))
			return null;
		
		DiscoInfo discoInfo = new DiscoInfo();
		discoInfo.getIdentities().add(new Identity("account", "registered"));
		
		return discoInfo;
	}

	@Override
	public DiscoItems discoItems(IProcessingContext context, Iq iq, JabberId jid, String node) {
		if (!imServerListener.isIMServer())
			return null;
		
		if (jid.isBareId() && domain.equals(jid.getDomain()) && node == null) {
			return discoAvailableResources(context, iq, jid);
		}
		
		return null;
	}

	private DiscoItems discoAvailableResources(IProcessingContext context, Iq iq, JabberId jid) {
		if (!authenticator.exists(jid.getNode()))
			return null;
		
		Subscription subscription = subscriptionService.get(context.getJid().getNode(), jid.getBareIdString());
		if (subscription.getState() != Subscription.State.FROM &&
				subscription.getState() != Subscription.State.BOTH)
			return null;
		
		IResource[] resources = resourceService.getResources(jid);
		
		DiscoItems discoItems = new DiscoItems();
		
		if (resources != null) {
			for (IResource resource : resources) {
				discoItems.getItems().add(new Item(resource.getJid()));
			}
		}
		
		return discoItems;
	}

	@Override
	public void setApplicationConfiguration(IApplicationConfiguration appConfiguration) {
		domain = appConfiguration.getDomainName();
	}

}
