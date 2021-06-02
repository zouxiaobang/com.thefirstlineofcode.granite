package com.firstlinecode.granite.framework.app.spring;

import org.pf4j.DefaultPluginFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;

import com.firstlinecode.granite.framework.core.app.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.app.IApplicationComponentServiceAware;

public class ApplicationPluginManager extends SpringPluginManager implements IApplicationComponentServiceAware {
	private IApplicationComponentService appComponentService;
	
	@Override
	protected PluginFactory createPluginFactory() {
		return new ApplicationPluginFactory();
	}
	

	
	private class ApplicationPluginFactory extends DefaultPluginFactory {
		@Override
		public Plugin create(PluginWrapper pluginWrapper) {
			Plugin plugin = super.create(pluginWrapper);
			if (plugin == null)
				return null;
			
			return appComponentService.inject(plugin);
		}		
	}



	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;		
	}
}
