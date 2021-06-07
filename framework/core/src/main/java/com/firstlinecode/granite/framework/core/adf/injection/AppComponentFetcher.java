package com.firstlinecode.granite.framework.core.adf.injection;

import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;

public class AppComponentFetcher implements IDependencyFetcher {
	private IApplicationComponentService appComponentService;
	private String componentId;
	
	public AppComponentFetcher(IApplicationComponentService appComponentService, String componentId) {
		this.appComponentService = appComponentService;
		this.componentId = componentId;
	}
	
	@Override
	public Object fetch() {
		Object component = appComponentService.getComponent(componentId);
		if (component == null)
			throw new IllegalArgumentException(String.format("No component which's component id is %s in repository.",
					componentId));
		
		return component;
	}

}
