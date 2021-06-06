package com.firstlinecode.granite.framework.core.repository;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.config.IConfigurationManager;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.IServerConfigurationAware;

public class ServiceWrapper implements IServiceWrapper {
	private IServerConfiguration serverConfiguration;
	private IConfigurationManager configurationManager;
	private IComponentInfo componentInfo;
	private IRepository repository;
	private IApplicationComponentService appComponentService;
	
	public ServiceWrapper(IServerConfiguration serverConfiguration, IConfigurationManager configurationManager,
			IRepository repository,IApplicationComponentService appComponentService,
				IComponentInfo componentInfo) {
		this.serverConfiguration = serverConfiguration;
		this.configurationManager = configurationManager;
		this.repository = repository;
		this.appComponentService = appComponentService;
		
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
		synchronized (repository) {
			return doCreateComponent(componentInfo);
		}
	}

	private Object doCreateComponent(IComponentInfo componentInfo)
			throws CreationException, Exception {
		
		Object component = repository.get(componentInfo.getId());
		
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
			((IServerConfigurationAware)component).setServerConfiguration(serverConfiguration);
		}
		
		if (component instanceof IConfigurationAware) {
			IConfiguration configuration = configurationManager.getConfiguration(componentInfo.getId());
			((IConfigurationAware)component).setConfiguration(configuration);
		}
		
		if (component instanceof IComponentIdAware) {
			((IComponentIdAware)component).setComponentId(componentInfo.getId());
		}
		
		if (component instanceof IApplicationComponentServiceAware) {
			((IApplicationComponentServiceAware)component).setApplicationComponentService(appComponentService);
		}
		
		if (component instanceof IInitializable) {
			((IInitializable)component).init();
		}
		
		if (componentInfo.isSingleton()) {
			repository.put(componentInfo.getId(), component);
		}
		
		return component;
	}
	
}
