package com.firstlinecode.granite.framework.core.internal.repository;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IConfigurationManager;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;
import com.firstlinecode.granite.framework.core.repository.CreationException;
import com.firstlinecode.granite.framework.core.repository.IComponentIdAware;
import com.firstlinecode.granite.framework.core.repository.IComponentInfo;
import com.firstlinecode.granite.framework.core.repository.IDependencyInfo;
import com.firstlinecode.granite.framework.core.repository.IInitializable;
import com.firstlinecode.granite.framework.core.repository.IServiceWrapper;
import com.firstlinecode.granite.framework.core.repository.ISingletonComponentHolder;
import com.firstlinecode.granite.framework.core.repository.ServiceCreationException;

public class ServiceWrapper implements IServiceWrapper {
	private IServerConfiguration serverConfiguration;
	private IConfigurationManager configurationManager;
	private IComponentInfo componentInfo;
	private ISingletonComponentHolder singletonHolder;
	
	public ServiceWrapper(IServerConfiguration serverConfiguration, IConfigurationManager configurationManager,
			ISingletonComponentHolder singletonHolder, IComponentInfo componentInfo) {
		this.serverConfiguration = serverConfiguration;
		this.configurationManager = configurationManager;
		this.singletonHolder = singletonHolder;
		
		if (!componentInfo.isService()) {
			throw new IllegalArgumentException(String.format("Component(id: %s) should be a service.",
					componentInfo.getId()));
		}
		
		this.componentInfo = componentInfo;
	}

	@Override
	public String getId() {
		return componentInfo.getId();
	}

	@Override
	public IService create() throws ServiceCreationException {
		try {
			return (IService)createComponent(componentInfo);
		} catch (Exception e) {
			throw new ServiceCreationException(String.format("Can't create service '%s'.",
					componentInfo.getId()), e);
		}
	}

	private Object createComponent(IComponentInfo componentInfo) throws Exception {
		synchronized (singletonHolder) {
			return doCreateComponent(componentInfo);
		}
	}

	private Object doCreateComponent(IComponentInfo componentInfo)
			throws CreationException, Exception {
		
		Object component = singletonHolder.get(componentInfo.getId());
		
		if (component != null)
			return component;
		
		component = componentInfo.create();
		for (IDependencyInfo dependency : componentInfo.getDependencies()) {
			for (IComponentInfo bindedIComponentInfo : dependency.getBindedComponents()) {
				Object bindedComponent = createComponent(bindedIComponentInfo);
				dependency.injectComponent(component, bindedComponent);
			}
		}
		
		if (component instanceof IServerConfigurationAware) {
			((IServerConfigurationAware)component).setApplicationConfiguration(serverConfiguration);
		}
		
		if (component instanceof IConfigurationAware) {
			IConfiguration configuration = configurationManager.getConfiguration(componentInfo.getId());
			((IConfigurationAware)component).setConfiguration(configuration);
		}
		
		if (component instanceof IComponentIdAware) {
			((IComponentIdAware)component).setComponentId(componentInfo.getId());
		}
		
		if (component instanceof IInitializable) {
			((IInitializable)component).init();
		}
		
		if (componentInfo.isSingleton()) {
			singletonHolder.put(componentInfo.getId(), component);
		}
		
		return component;
	}
	
}
