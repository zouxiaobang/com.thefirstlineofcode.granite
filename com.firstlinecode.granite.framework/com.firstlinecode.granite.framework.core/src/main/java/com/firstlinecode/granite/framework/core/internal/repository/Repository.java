package com.firstlinecode.granite.framework.core.internal.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.commons.osgi.OsgiUtils;
import com.firstlinecode.granite.framework.core.commons.utils.ContributionClass;
import com.firstlinecode.granite.framework.core.commons.utils.ContributionClassTrackHelper;
import com.firstlinecode.granite.framework.core.commons.utils.IContributionClassTracker;
import com.firstlinecode.granite.framework.core.commons.utils.IoUtils;
import com.firstlinecode.granite.framework.core.config.IApplicationConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationManager;
import com.firstlinecode.granite.framework.core.internal.config.LocalFileConfigurationManager;
import com.firstlinecode.granite.framework.core.repository.IComponentCollector;
import com.firstlinecode.granite.framework.core.repository.IComponentInfo;
import com.firstlinecode.granite.framework.core.repository.IComponentQueryer;
import com.firstlinecode.granite.framework.core.repository.IDependencyInfo;
import com.firstlinecode.granite.framework.core.repository.IDestroyable;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.repository.IServiceListener;
import com.firstlinecode.granite.framework.core.repository.IServiceWrapper;
import com.firstlinecode.granite.framework.core.repository.ISingletonHolder;

public class Repository implements IRepository, IContributionClassTracker<Component>,
			IComponentCollector, IComponentQueryer, ISingletonHolder {
	private static final String KEY_GRANITE_COMPONENT_SCAN = "Granite-Component-Scan";
	private static final String KEY_GRANITE_COMPONENT_SCAN_PATHS = "Granite-Component-Scan-Paths";
	
	private static final Logger logger = LoggerFactory.getLogger(Repository.class);
	
	private BundleContext bundleContext;
	private List<IServiceListener> serviceListeners;
	
	private IApplicationConfiguration appConfiguration;
	
	private Map<String, String[]> componentBindings;
	
	private Map<String, IComponentInfo> components;
	
	private List<String> availableServices;
	
	private IConfigurationManager configurationManager;
	
	private ContributionClassTrackHelper<Component> trackHelper;
	
	private Map<String, Object> singletons;
	
	private ServiceRegistration<IComponentCollector> srComponentCollector;
	private ServiceRegistration<IRepository> srRepository;
	
	public Repository(BundleContext bundleContext, IApplicationConfiguration appConfiguration) {
		this.bundleContext = bundleContext;
		this.appConfiguration = appConfiguration;
		
		serviceListeners = new ArrayList<>();
		
		componentBindings = new HashMap<>();
		
		components = new HashMap<>();
		availableServices = new ArrayList<>();
		
		singletons = new HashMap<>();
	}
	
	@Override
	public void init() {
		readComponentBindings();
		createConfigurationManager();
		
		trackHelper = new ContributionClassTrackHelper<>(bundleContext,
				KEY_GRANITE_COMPONENT_SCAN, KEY_GRANITE_COMPONENT_SCAN_PATHS,
					Component.class, this);
		trackHelper.track();
		
		exportOsgiServices();
	}

	private void exportOsgiServices() {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(Constants.SERVICE_INTENTS, IRepository.GRANITE_FRAMEWORK_COMPONENT_COLLECTOR);
		srComponentCollector = bundleContext.registerService(IComponentCollector.class, this, properties);
		
		srRepository = bundleContext.registerService(IRepository.class, this, null);
	}

	private void createConfigurationManager() {
		String symbolicName = appConfiguration.getConfigurationManagerBundleSymbolicName();
		String className = appConfiguration.getConfigurationManagerClass();
		if (symbolicName != null && className != null) {
			for (Bundle bundle : bundleContext.getBundles()) {
				if (bundle.getSymbolicName().equals(symbolicName) && bundle.getState() != Bundle.ACTIVE) {
					try {
						bundle.start();
						configurationManager = OsgiUtils.createInstance(bundle, className);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					
					break;
				}
			}
		} else {
			configurationManager = new LocalFileConfigurationManager(appConfiguration.getConfigDir());
		}
		
		if (configurationManager == null) {
			throw new RuntimeException("Null configuration manager.");
		}
	}

	private void readComponentBindings() {
		Properties properties = new Properties();
		Reader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(appConfiguration.getComponentBindingProfile()));
			properties.load(reader);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't read component binding profile: %s.",
					appConfiguration.getComponentBindingProfile()), e);
		} finally {
			IoUtils.closeIO(reader);
		}
		
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			
			StringTokenizer tokenizer = new StringTokenizer(value, ",");
			String[] dependeices = new String[tokenizer.countTokens()];
			
			int countToken = tokenizer.countTokens();
			for (int i = 0; i < countToken; i++) {
				dependeices[i] = tokenizer.nextToken();
			}
			
			componentBindings.put(key, dependeices);
		}
	}
	
	@Override
	public void destroy() {
		srRepository.unregister();
		srComponentCollector.unregister();
		
		trackHelper.stopTrack();
	}
	
	@Override
	public void addServiceListener(IServiceListener listener) {
		serviceListeners.add(listener);
	}
	
	@Override
	public void removeServiceListener(IServiceListener listener) {
		serviceListeners.remove(listener);
	}

	@Override
	public synchronized void found(ContributionClass<Component> cc) {
		BundleContext bundleContext = cc.getBundleContext();
		Class<?> clazz = cc.getType();
		Component componentAnnotation = cc.getAnnotation();
		if (componentAnnotation != null) {
			IComponentInfo componentInfo = new GenericComponentInfo(componentAnnotation.value(), clazz, bundleContext);
			
			if (components.containsKey(componentInfo.getId())) {
				throw new RuntimeException(String.format("Component id[%s] has already existed.",
						componentInfo.getId()));
			}
			
			logger.debug("Component {} found.", componentInfo);
			
			scanDependencies(clazz, componentInfo);
			
			componentFound(componentInfo);
			
			IComponentInfo[] aliasComponents = getAliasComponents(componentInfo, componentAnnotation);
			for (IComponentInfo aliasComponent : aliasComponents) {
				componentFound(aliasComponent);
			}
		}
	}

	private IComponentInfo[] getAliasComponents(IComponentInfo componentInfo, Component componentAnnotation) {
		String[] alias = componentAnnotation.alias();
		if (alias.length == 0)
			return new IComponentInfo[0];
		
		IComponentInfo[] aliasComponents = new IComponentInfo[alias.length];
		
		for (int i = 0; i < alias.length; i++) {
			IComponentInfo aliasComponent = componentInfo.getAliasComponent(alias[i]);
			
			for (IDependencyInfo dependency : componentInfo.getDependencies()) {
				int dependenciesCount = getBindedDependenciesCount(getFullDependencyId(alias[i], dependency.getBareId()));
				aliasComponent.addDependency(dependency.getAliasDependency(alias[i], dependenciesCount));
			}
			
			aliasComponents[i] = aliasComponent;
		}
		
		return aliasComponents;
	}

	private void processAvailables(IComponentInfo componentInfo) {
		if (componentInfo.isService() && !availableServices.contains(componentInfo.getId())) {
			availableServiceFound(componentInfo);
		} else {
			for (IComponentInfo component : components.values()) {
				if (component.isService() && component.isAvailable() &&
						!availableServices.contains(component.getId())) {
					availableServiceFound(component);
				}
			}
		}
	}

	private void availableServiceFound(IComponentInfo componentInfo) {
		availableServices.add(componentInfo.getId());
		
		logger.info("Service {} is available.", componentInfo);
		
		IServiceWrapper serviceWrapper = new ServiceWrapper(appConfiguration, configurationManager,
				this, componentInfo);
		for (IServiceListener listener : serviceListeners) {
			listener.available(serviceWrapper);
		}
	}

	private void bindDependencies(IComponentInfo newComponent) {
		for (IDependencyInfo dependencyOfNewComponent : newComponent.getDependencies()) {
			String[] bindedComponentIds = componentBindings.get(dependencyOfNewComponent.getId());
			if (bindedComponentIds == null)
				continue;
			
			for (String bindedComponentId : bindedComponentIds) {
				newComponentDependencyBinding(newComponent, dependencyOfNewComponent, bindedComponentId);
			}
		}
		
		for (IComponentInfo existedComponent : components.values()) {
			for (IDependencyInfo dependencyOfExistedComponent : existedComponent.getDependencies()) {
				String[] bindedComponentIds = componentBindings.get(dependencyOfExistedComponent.getId());
				if (bindedComponentIds == null)
					continue;
				
				for (String bindedComponentId : bindedComponentIds) {
					existedComponentDependencyBinding(newComponent, existedComponent, dependencyOfExistedComponent, bindedComponentId);				
				}
			}
		}
	}

	private void existedComponentDependencyBinding(IComponentInfo newComponent, IComponentInfo exitedComponent,
				IDependencyInfo dependencyOfExistedComponent, String bindedComponentId) {
		if (newComponent.getId().equals(bindedComponentId)) {
			if (newComponent.isService()) {
				logger.warn("Component {} depends on a component {} which implements IService interface. It isn't allowed. we ignore this binding.",
						exitedComponent, newComponent);
			} else {
				logger.debug("Component binding: bind {} to {}.", newComponent, dependencyOfExistedComponent);
				dependencyOfExistedComponent.addBindedComponent(newComponent);
			}
		}
	}

	private void newComponentDependencyBinding(IComponentInfo newComponent, IDependencyInfo dependency, String bindedComponentId) {
		for (IComponentInfo existedComponent : components.values()) {
			if (existedComponent.getId().equals(bindedComponentId)) {
				if (existedComponent.isService()) {
					logger.warn("Component {} depends on a component {} which implements IService interface. It isn't allowed. we ignore this binding.",
							newComponent, existedComponent);
				}
				
				logger.debug("Component binding: bind {} to {}.", existedComponent, dependency);
				dependency.addBindedComponent(existedComponent);
				break;
			}
		}
	}

	public static String getFullDependencyId(String referencerId, String id) {
		return referencerId + SEPARATOR_COMPONENT_DEPENDENCY_PATH + id;
	}

	private void scanDependencies(Class<?> clazz, IComponentInfo componentInfo) {
		for (Field field : clazz.getDeclaredFields()) {
			Dependency dependencyAnnotation = field.getAnnotation(Dependency.class);
			if (dependencyAnnotation == null) {
				continue;
			}
			
			IDependencyInfo dependencyInfo = null;
			String id = getFullDependencyId(componentInfo.getId(), dependencyAnnotation.value());
			int bindedComponentsCount = getBindedDependenciesCount(id);
			if (List.class.isAssignableFrom(field.getType())) {
				dependencyInfo = new ListFieldDependencyInfo(id, dependencyAnnotation.value(),
						null, field, bindedComponentsCount);
			} else {
				if (bindedComponentsCount > 1) {
					throw new RuntimeException("Binded components count of field dependency must be 0 or 1.");
				}
				
				boolean notNull = bindedComponentsCount > 0;
				dependencyInfo = new FieldDependencyInfo(id, dependencyAnnotation.value(),
						field.getType(), field, notNull);
			}
			
			logger.debug("Dependency {} found.", dependencyInfo);
			componentInfo.addDependency(dependencyInfo);				
			
		}
		
		for (Method method : clazz.getMethods()) {
			if (method.getDeclaringClass().equals(Object.class))
				continue;
			
			int modifiers = method.getModifiers();
			if (!Modifier.isPublic(modifiers) ||
					Modifier.isAbstract(modifiers) ||
					Modifier.isStatic(modifiers))
				continue;
			
			Dependency dependencyAnnotation = method.getAnnotation(Dependency.class);
			if (dependencyAnnotation == null) {
				continue;
			}
			
			String id = getFullDependencyId(componentInfo.getId(), dependencyAnnotation.value());
			int bindedComponentsCount = getBindedDependenciesCount(id);
			if (isSetterMethod(method)) {
				if (bindedComponentsCount > 1) {
					throw new RuntimeException("Binded components count of setter method dependency must be 0 or 1.");
				}
				
				Class<?> type = method.getParameterTypes()[0];
				IDependencyInfo dependencyInfo = new MethodDependencyInfo(id, dependencyAnnotation.value(),
						type, method, bindedComponentsCount);
				
				logger.debug("Dependency {} found.", dependencyInfo);
				componentInfo.addDependency(dependencyInfo);
			} else if (isAddMethod(method)) {
				Class<?> type = method.getParameterTypes()[0];
				
				IDependencyInfo dependencyInfo = new MethodDependencyInfo(id, dependencyAnnotation.value(),
						type, method, bindedComponentsCount);
				
				logger.debug("Dependency {} found.", dependencyInfo);
				componentInfo.addDependency(dependencyInfo);
			} else {
				logger.warn("Method annotated with @Dependency isn't a valid setter or add method.");
			}
			
			
		}
	}

	private int getBindedDependenciesCount(String id) {
		String[] dependencies = componentBindings.get(id);
		if (dependencies == null)
			return 0;
		
		return dependencies.length;
	}

	private boolean isSetterMethod(Method method) {
		String methodName = method.getName();
		if (methodName.length() > 4 && methodName.startsWith("set") && Character.isUpperCase(methodName.charAt(3))) {
			Class<?>[] types = method.getParameterTypes();
			if (types.length != 1)
				return false;
			
			if (types[0].isPrimitive())
				return false;
			
			return true;
		}
		
		return false;
	}
	
	private boolean isAddMethod(Method method) {
		String methodName = method.getName();
		if (methodName.length() > 4 && methodName.startsWith("add") && Character.isUpperCase(methodName.charAt(3))) {
			Class<?>[] types = method.getParameterTypes();
			if (types.length != 1)
				return false;
			
			if (types[0].isPrimitive())
				return false;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void lost(ContributionClass<Component> cc) {
		String id = cc.getAnnotation().value();
		componentLost(id);
		
		String[] alias = cc.getAnnotation().alias();
		for (int i = 0; i < alias.length; i++) {
			componentLost(alias[i]);
		}
	}
	
	private void unbindDependencies(IComponentInfo componentInfo) {
		for (IComponentInfo component : components.values()) {
			for (IDependencyInfo dependency : component.getDependencies()) {
				for (IComponentInfo bindedComponent : dependency.getBindedComponents()) {
					if (bindedComponent.equals(componentInfo)) {
						dependency.removeBindedComponent(componentInfo);
						break;
					}
				}
			}
		}
	}

	private void unavailableServiceFound(String serviceId) {
		availableServices.remove(serviceId);
		
		for (IServiceListener listener : serviceListeners) {
			listener.unavailable(serviceId);
		}
	}

	@Override
	public synchronized void componentFound(IComponentInfo componentInfo) {
		bindDependencies(componentInfo);
		
		components.put(componentInfo.getId(), componentInfo);
		
		if (componentInfo.isAvailable()) {
			processAvailables(componentInfo);
		}
	}

	@Override
	public synchronized void componentLost(String componentId) {
		IComponentInfo component = components.get(componentId);
		if (component.isService()) {
			if (availableServices.contains(componentId)) {
				unavailableServiceFound(componentId);
			}
		} else {
			unbindDependencies(component);
			
			// find all unavailable services and remove them from available services list
			List<String> unavailables = new ArrayList<>();
			for (String serviceId : availableServices) {
				IComponentInfo service = components.get(serviceId);
				if (!service.isAvailable()) {
					unavailables.add(serviceId);
				}
			}
			
			for (String serviceId : unavailables) {
				unavailableServiceFound(serviceId);
			}
		}
		
		if (component instanceof IDestroyable) {
			((IDestroyable)component).destroy();
		}
		
		components.remove(componentId);
	}

	@Override
	public synchronized IComponentInfo[] getServices() {
		List<IComponentInfo> services = new ArrayList<>();
		
		for (IComponentInfo ci : components.values()) {
			if (ci.isService()) {
				services.add(ci);
			}
		}
		
		return services.toArray(new IComponentInfo[services.size()]);
	}

	@Override
	public synchronized IComponentInfo getService(String serviceId) {
		if (serviceId == null)
			throw new RuntimeException("Null service id.");
		
		for (IComponentInfo ci : getServices()) {
			if (serviceId.equals(ci.getId()))
				return ci;
		}
		
		return null;
	}

	@Override
	public synchronized IComponentInfo[] getComponents() {
		return components.values().toArray(new IComponentInfo[components.size()]);
	}

	@Override
	public String[] getComponentBinding(String componentId) {
		return componentBindings.get(componentId);
	}

	@Override
	public void put(String id, Object component) {
		singletons.put(id, component);
	}

	@Override
	public Object get(String id) {
		return singletons.get(id);
	}
}
