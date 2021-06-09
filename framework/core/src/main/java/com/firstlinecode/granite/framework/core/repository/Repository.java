package com.firstlinecode.granite.framework.core.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.framework.core.IService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.commons.utils.IoUtils;
import com.firstlinecode.granite.framework.core.config.IConfigurationManager;
import com.firstlinecode.granite.framework.core.config.IServerConfiguration;
import com.firstlinecode.granite.framework.core.config.LocalFileConfigurationManager;

public class Repository implements IRepository {
	private static final String CLASS_FILE_EXTENSION_NAME = ".class";
	private static final String JAR_FILE_EXTENSION_NAME = ".jar";
	public static final String GRANITE_LIBRARY_NAME_PREFIX = "granite-";
	public static final String SAND_LIBRARY_NAME_PREFIX = "sand-";

	private static final Logger logger = LoggerFactory.getLogger(Repository.class);
	
	private IServiceListener serviceListener;
	private IServerConfiguration serverConfiguration;	
	private Map<String, String[]> componentBindings;
	private Map<String, Object> singletons;
	
	private Map<String, IComponentInfo> componentInfos;
	private List<String> availableServices;
	private IConfigurationManager configurationManager;
	private IApplicationComponentService appComponentService;
	
	public Repository(IServerConfiguration serverConfiguration, IApplicationComponentService appComponentService) {
		this.serverConfiguration = serverConfiguration;
		this.appComponentService = appComponentService;
		
		componentBindings = new HashMap<>();
		
		componentInfos = new HashMap<>();
		availableServices = new ArrayList<>();
		
		singletons = new HashMap<>();
	}
	
	@Override
	public void init() {
		readComponentBindings();
		createConfigurationManager();
		
		loadSystemComponents();
		loadExtendedComponents();
		
		if (appComponentService instanceof IRepositoryAware) {
			((IRepositoryAware)appComponentService).setRepository(this);
		}
		
		processAvailableServices();
	}
	
	private void loadExtendedComponents() {
		List<Class<? extends IComponentContributor>> componentContributorClasses = appComponentService.getExtensionClasses(IComponentContributor.class);
		for (Class<? extends IComponentContributor> comonentContributorClass : componentContributorClasses) {
			IComponentContributor componentContributor = appComponentService.createExtension(comonentContributorClass);
			Class<?>[] componentClasses = componentContributor.getComponentClasses();
			if (componentClasses == null || componentClasses.length == 0)
				continue;
			
			for (Class<?> componentClass : componentClasses) {				
				Annotation[] annotations = componentClass.getAnnotations();
				for (Annotation annotation : annotations) {
					if (annotation instanceof Component) {
						found(componentClass, (Component)annotation);
						continue;
					}
					
					if (logger.isWarnEnabled()) {
						logger.warn("Component class {} didn't load as a component because it isn't annotated by @Component. Please check your code.",
								componentClass.getName());
					}
				}
			}
		}
	}

	private void loadSystemComponents() {
		File libsDir = new File(serverConfiguration.getSystemLibsDir());
		if (!libsDir.exists() || !libsDir.isDirectory())
			throw new IllegalArgumentException(String.format("Can't determine system libraries directory. %s doesn't exist or isn't a directory.", libsDir));
		
		loadCoreComponents(libsDir);
		loadCustomizedComponents(libsDir);
	}

	private void loadCustomizedComponents(File libsDir) {
		if (serverConfiguration.getCustomizedLibraries() == null ||
				serverConfiguration.getCustomizedLibraries().length == 0) {
			if (logger.isInfoEnabled())
				logger.info("No customized libraries found.");
			return;
		}
		
		for (String library: serverConfiguration.getCustomizedLibraries()) {
			File libraryFile = getLibraryFile(libsDir, library);
			loadComponentsFromLibrary(libraryFile);			
		}
		
		if (logger.isInfoEnabled()) {
			logger.info("Components has loaded from customized libraries.");
		}

	}

	private File getLibraryFile(File libsDir, String library) {
		for (File child : libsDir.listFiles()) {
			if (!child.isFile())
				continue;
			
			String path = child.getPath();
			if (path.startsWith(library) && path.endsWith(JAR_FILE_EXTENSION_NAME)) {
				if (logger.isInfoEnabled())
					logger.info("Cutomized library {} has be found. The library file name is {}.", library, path);
				
				return child;
			}
		}
		
		if (logger.isWarnEnabled())
			logger.warn("Customized library {} hasn't be found. Please check your server configuration.");
		
		return null;
	}

	private void loadCoreComponents(File libsDir) {
		File[] coreLibraryFiles = libsDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.startsWith(GRANITE_LIBRARY_NAME_PREFIX) ||
						name.startsWith(SAND_LIBRARY_NAME_PREFIX)) &&
						name.endsWith(JAR_FILE_EXTENSION_NAME);
			}
		});
		
		for (File libraryFile : coreLibraryFiles) {
			loadComponentsFromLibrary(libraryFile);
		}
		if (logger.isInfoEnabled()) {
			logger.info("Components has loaded from core libraries.");
		}
	}
	
	private void loadComponentsFromLibrary(File libraryFile) {
		Set<String> classNames = new HashSet<>();
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(libraryFile);
			Enumeration<JarEntry> enumeration = jarFile.entries();
			while (enumeration.hasMoreElements()) {
				JarEntry entry = enumeration.nextElement();
				String entryName = entry.getName();
				if (!entryName.endsWith(CLASS_FILE_EXTENSION_NAME))
					continue;
				
				String className = entryName.replaceAll("/", ".").substring(0, entryName.length() - 6);
				classNames.add(className);
			}
			
			classNames.forEach((sClass) -> {
				try {
					Class<?> clazz = Class.forName(sClass);
					Annotation[] annotations = clazz.getAnnotations();
					for (Annotation annotation : annotations) {
						if (annotation instanceof Component) {
							found(clazz, (Component)annotation);
						}
							
					}
				} catch (ClassNotFoundException e) {
					if (logger.isWarnEnabled())
						logger.warn("Can't load class[name: {}] from system library.", sClass, e);
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(String.format("Failed to load components from library file %s.", libraryFile.getPath()), e);
		} finally {
			if (jarFile != null)
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	private void createConfigurationManager() {
		configurationManager = new LocalFileConfigurationManager(serverConfiguration.getConfigurationDir());
		
		if (configurationManager == null) {
			throw new RuntimeException("Null configuration manager.");
		}
	}

	private void readComponentBindings() {
		Properties properties = new Properties();
		Reader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(serverConfiguration.getComponentBindingProfile()));
			properties.load(reader);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't read component binding profile: %s.",
					serverConfiguration.getComponentBindingProfile()), e);
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
				dependeices[i] = tokenizer.nextToken().trim();
			}
			
			componentBindings.put(key, dependeices);
		}
	}
	
	@Override
	public void setServiceListener(IServiceListener serviceListener) {
		this.serviceListener = serviceListener;
	}
	
	@Override
	public void removeServiceListener() {	
		serviceListener = null;
	}

	public void found(Class<?> type, Component componentAnnotation) {
		if (componentAnnotation != null) {
			IComponentInfo componentInfo = new GenericComponentInfo(componentAnnotation.value(), type);
			
			if (componentInfos.containsKey(componentInfo.getId())) {
				throw new RuntimeException(String.format("Reduplicated component. Component which's id is %s has already existed.",
						componentInfo.getId()));
			}
			
			if (logger.isDebugEnabled())
				logger.debug("Component {} has found.", componentInfo.getId());
			
			scanDependencies(type, componentInfo);
			
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

	private void processAvailableServices() {
		for (IComponentInfo component : componentInfos.values()) {
			if (component.isService() && component.isAvailable() &&
					!availableServices.contains(component.getId())) {
				availableServiceFound(component);
			}
		}
	}

	private void availableServiceFound(IComponentInfo componentInfo) {
		availableServices.add(componentInfo.getId());
		
		logger.info("Service {} is available.", componentInfo);
		
		IServiceWrapper serviceWrapper = new ServiceWrapper(serverConfiguration, configurationManager,
				this, appComponentService, componentInfo);
		serviceListener.available(serviceWrapper);
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
		
		for (IComponentInfo existedComponent : componentInfos.values()) {
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
		for (IComponentInfo existedComponent : componentInfos.values()) {
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
		for (Field field : getClassFields(clazz, null)) {
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

	private List<Field> getClassFields(Class<?> clazz, List<Field> fields) {
		if (fields == null)
			fields = new ArrayList<Field>();
		
		for (Field field : clazz.getDeclaredFields()) {
			fields.add(field);
		}
		
		Class<?> parent = clazz.getSuperclass();
		if (parent.getAnnotation(Component.class) == null)
			return fields;
		
		return getClassFields(parent, fields);
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

	public void componentFound(IComponentInfo componentInfo) {
		bindDependencies(componentInfo);
		componentInfos.put(componentInfo.getId(), componentInfo);
	}

	@Override
	public IComponentInfo[] getServiceInfos() {
		List<IComponentInfo> serviceInfos = new ArrayList<>();
		
		for (IComponentInfo ci : componentInfos.values()) {
			if (ci.isService()) {
				serviceInfos.add(ci);
			}
		}
		
		return serviceInfos.toArray(new IComponentInfo[serviceInfos.size()]);
	}

	@Override
	public IComponentInfo getServiceInfo(String serviceId) {
		if (serviceId == null)
			throw new RuntimeException("Null service id.");
		
		IComponentInfo serviceInfo = getComponentInfo(serviceId);
		if (IService.class.isAssignableFrom(serviceInfo.getType()))
			return serviceInfo;
		
		return null;
	}
	
	@Override
	public IComponentInfo getComponentInfo(String componentId) {
		for (IComponentInfo ci : getComponentInfos()) {
			if (componentId.equals(ci.getId()))
				return ci;
		}
		
		return null;
	}

	@Override
	public IComponentInfo[] getComponentInfos() {
		return componentInfos.values().toArray(new IComponentInfo[componentInfos.size()]);
	}

	@Override
	public String[] getComponentBinding(String componentId) {
		return componentBindings.get(componentId);
	}

	@Override
	public void putSingleton(String id, Object component) {
		singletons.put(id, component);
	}

	@Override
	public Object get(String id) {
		Object component = singletons.get(id);
		if (component == null) {
			IComponentInfo componentInfo = getComponentInfo(id);
			if (componentInfo == null)
				return component;
			
			try {
				component = componentInfo.create();
			} catch (CreationException e) {
				throw new RuntimeException(String.format("Can't create component which's component info is %s.", componentInfo), e);
			}
		}
		
		return component;
	}
}
