package com.firstlinecode.granite.lite.xeps.disco;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stanza.error.ItemNotFound;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.granite.framework.core.annotations.AppComponent;
import com.firstlinecode.granite.framework.core.commons.osgi.IBundleContextAware;
import com.firstlinecode.granite.framework.core.commons.osgi.IContributionTracker;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.supports.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.processing.IProcessingContext;
import com.firstlinecode.granite.xeps.disco.IDiscoProcessor;
import com.firstlinecode.granite.xeps.disco.IDiscoProvider;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.disco.Feature;
import com.firstlinecode.basalt.xeps.disco.Identity;
import com.firstlinecode.basalt.xeps.disco.Item;

@AppComponent("disco.processor")
public class DiscoProcessor implements IDiscoProcessor, IBundleContextAware, IInitializable, IConfigurationAware {
	private static final String CONFIGURATION_KEY_DISABLE_ITEM_NOT_FOUND = "disable.item.not.found";

	private static final String KEY_GRANITE_DISCO_PROVIDERS = "Granite-Disco-Providers";
	
	private static final String SEPARATOR_PROVIDERS = ",";

	private BundleContext bundleContext;
	
	private Map<String, List<IDiscoProvider>> bundleAndDiscoProviders = new ConcurrentHashMap<>();
	
	private IApplicationComponentService appComponentService;
	
	private boolean disableItemNotFound;
	
	@Override
	public void discoInfo(IProcessingContext context, Iq iq, JabberId jid, String node) {
		if (iq.getType() == Iq.Type.GET) {
			doDiscoInfo(context, iq, jid, node);
		} else if (iq.getType() == Iq.Type.RESULT) {
			deliverDiscoInfo(context, iq, jid, node);
		} else {
			throw new ProtocolException(new BadRequest("IQ type must be set to 'get' or 'result'."));
		}
	}

	private void deliverDiscoInfo(IProcessingContext context, Iq iq, JabberId jid, String node) {
		// TODO Auto-generated method stub
		
	}

	private void doDiscoInfo(IProcessingContext context, Iq iq, JabberId jid, String node) {
		if (iq.getType() == Iq.Type.GET) {
			doDiscoItems(context, iq, jid, node);
		} else if (iq.getType() == Iq.Type.RESULT) {
			deliverDiscoItems(context, iq, jid, node);
		} else {
			throw new ProtocolException(new BadRequest("IQ type must be set to 'get' or 'result'."));
		}
		
	}

	private void deliverDiscoItems(IProcessingContext context, Iq iq, JabberId jid, String node) {
		// TODO Auto-generated method stub
		
	}

	private void doDiscoItems(IProcessingContext context, Iq iq, JabberId jid, String node) {
		DiscoInfo discoInfo = new DiscoInfo();
		boolean itemNotFound = true;
		for (List<IDiscoProvider> discoProviders : bundleAndDiscoProviders.values()) {
			for (IDiscoProvider discoProvider : discoProviders) {
				DiscoInfo partOfDiscoInfo = discoProvider.discoInfo(context, iq, jid, node);
				if (partOfDiscoInfo != null) {
					itemNotFound = false;
					for (Identity identity : partOfDiscoInfo.getIdentities()) {
						if (!discoInfo.getIdentities().contains(identity)) {
							discoInfo.getIdentities().add(identity);
						}
					}
					
					for (Feature feature : partOfDiscoInfo.getFeatures()) {
						if (!discoInfo.getFeatures().contains(feature)) {
							discoInfo.getFeatures().add(feature);
						}
					}
					
					if (partOfDiscoInfo.getXData() != null) {
						discoInfo.setXData(partOfDiscoInfo.getXData());
					}
					
				}
			}
		}
		
		processItemNotFound(itemNotFound);
		
		Iq result = new Iq(discoInfo, Iq.Type.RESULT, iq.getId());
		result.setFrom(jid);
		
		context.write(result);
	}

	private void processItemNotFound(boolean itemNotFound) {
		if (!itemNotFound)
			return;
		
		if (disableItemNotFound) {
			throw new ProtocolException(new ServiceUnavailable());
		} else {
			throw new ProtocolException(new ItemNotFound());
		}
	}
	
	@Override
	public void discoItems(IProcessingContext context, Iq iq, JabberId jid, String node) {
		DiscoItems discoItems = new DiscoItems();
		boolean itemNotFound = true;
		for (List<IDiscoProvider> discoProviders : bundleAndDiscoProviders.values()) {
			for (IDiscoProvider discoProvider : discoProviders) {
				DiscoItems partOfDiscoItems = discoProvider.discoItems(context, iq, jid, node);
				if (partOfDiscoItems != null) {
					itemNotFound = false;
					
					for (Item item : partOfDiscoItems.getItems()) {
						if (!discoItems.getItems().contains(item)) {
							discoItems.getItems().add(item);
						}
					}
					
					if (partOfDiscoItems.getSet() != null) {
						discoItems.setSet(partOfDiscoItems.getSet());
						// single provider provides all data. so break loop
						break;
					}
				}
			}
		}
		
		if (node != null) {
			discoItems.setNode(node);
		}
		
		processItemNotFound(itemNotFound);
		
		Iq result = new Iq(discoItems, Iq.Type.RESULT, iq.getId());
		result.setFrom(jid);
		
		context.write(result);
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public synchronized void init() {
		appComponentService = OsgiUtils.getService(bundleContext, IApplicationComponentService.class);
		OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_DISCO_PROVIDERS, new DiscoProvidersContributionTracker());
	}
	
	private class DiscoProvidersContributionTracker implements IContributionTracker {

		@Override
		public void found(Bundle bundle, String contribution) throws Exception {
			StringTokenizer st = new StringTokenizer(contribution, SEPARATOR_PROVIDERS);
			
			List<IDiscoProvider> discoProviders = new ArrayList<>(); 
			while (st.hasMoreTokens()) {
				String sProvider = st.nextToken();
				
				Class<?> clazz = bundle.loadClass(sProvider);
				
				if (!(IDiscoProvider.class.isAssignableFrom(clazz))) {
					throw new IllegalArgumentException(String.format("%s must implement %s[register disco provider].",
							sProvider, IDiscoProvider.class));
				}
				
				IDiscoProvider discoProvider;
				try {
					discoProvider = (IDiscoProvider)clazz.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("Can't instantiate disco provider.", e);
				}
				appComponentService.inject(discoProvider, bundle.getBundleContext());
				
				discoProviders.add(discoProvider);
			}
			
			if (discoProviders.size() > 0) {
				bundleAndDiscoProviders.put(bundle.getSymbolicName(), discoProviders);
			}
		}

		@Override
		public void lost(Bundle bundle, String contribution) throws Exception {
			List<IDiscoProvider> discoProviders = bundleAndDiscoProviders.get(bundle.getSymbolicName());
			
			if (discoProviders != null)
				bundleAndDiscoProviders.remove(bundle.getSymbolicName());
		}
		
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		disableItemNotFound = configuration.getBoolean(CONFIGURATION_KEY_DISABLE_ITEM_NOT_FOUND, true);
	}
}
