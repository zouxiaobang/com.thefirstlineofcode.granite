package com.firstlinecode.granite.lite.xeps.disco;

import org.pf4j.Extension;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.disco.Identity;
import com.firstlinecode.basalt.xeps.disco.Item;
import com.firstlinecode.granite.framework.core.annotations.BeanDependency;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.IAuthenticator;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.pipeline.processing.IProcessingContext;
import com.firstlinecode.granite.framework.im.IResource;
import com.firstlinecode.granite.framework.im.IResourcesService;
import com.firstlinecode.granite.framework.im.ISubscriptionService;
import com.firstlinecode.granite.framework.im.Subscription;
import com.firstlinecode.granite.xeps.disco.IDiscoProvider;

@Extension
public class ImClientDiscoProvider implements IDiscoProvider, IServerConfigurationAware {
	@Dependency("standard.im.server.listener")
	private StandardImServerListener imServerListener;
	
	@BeanDependency
	private IAuthenticator authenticator;
	
	@BeanDependency
	private ISubscriptionService subscriptionService;
	
	@BeanDependency
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
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domain = serverConfiguration.getDomainName();
	}

}
