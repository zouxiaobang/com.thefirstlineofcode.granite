package com.thefirstlineofcode.granite.framework.core.repository;

import com.thefirstlineofcode.granite.framework.core.IService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.config.IComponentConfigurations;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;

public class ServiceWrapper implements IServiceWrapper {
	private IServerConfiguration serverConfiguration;
	private IComponentConfigurations componentConfigurations;
	private IComponentInfo componentInfo;
	private IRepository repository;
	private IApplicationComponentService appComponentService;
	
	public ServiceWrapper(IServerConfiguration serverConfiguration, IComponentConfigurations componentConfigurations,
			IRepository repository, IApplicationComponentService appComponentService, IComponentInfo componentInfo) {
		this.serverConfiguration = serverConfiguration;
		this.componentConfigurations = componentConfigurations;
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
		
		if (component == null) {
			throw new RuntimeException(String.format("Component which's ID is '%s' not be found.", componentInfo.getId()));
		}
		
		for (IDependencyInfo dependency : componentInfo.getDependencies()) {
			for (IComponentInfo bindedIComponentInfo : dependency.getBindedComponents()) {
				Object bindedComponent = createComponent(bindedIComponentInfo);
				dependency.injectDependency(component, bindedComponent);
			}
		}
		
		injectByAwareInterfaces(componentInfo, component);
		
		return component;
	}

	private void injectByAwareInterfaces(IComponentInfo componentInfo, Object component) {
		if (component instanceof IServerConfigurationAware) {
			((IServerConfigurationAware)component).setServerConfiguration(serverConfiguration);
		}
		
		if (component instanceof IConfigurationAware) {
			IConfiguration configuration = componentConfigurations.getConfiguration(componentInfo.getId());
			((IConfigurationAware)component).setConfiguration(configuration);
		}
		
		if (component instanceof IComponentIdAware) {
			((IComponentIdAware)component).setComponentId(componentInfo.getId());
		}
		
		if (component instanceof IRepositoryAware) {
			((IRepositoryAware)component).setRepository(repository);
		}
		
		if (component instanceof IApplicationComponentServiceAware) {
			((IApplicationComponentServiceAware)component).setApplicationComponentService(appComponentService);
		}
		
		if (component instanceof IInitializable) {
			((IInitializable)component).init();
		}
	}
}
