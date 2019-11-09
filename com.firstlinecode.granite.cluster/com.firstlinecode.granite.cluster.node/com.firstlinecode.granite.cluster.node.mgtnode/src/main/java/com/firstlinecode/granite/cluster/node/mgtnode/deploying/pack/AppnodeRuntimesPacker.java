package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.commons.deploying.NodeType;
import com.firstlinecode.granite.cluster.node.commons.utils.IoUtils;
import com.firstlinecode.granite.cluster.node.commons.utils.SectionalProperties;
import com.firstlinecode.granite.cluster.node.commons.utils.StringUtils;
import com.firstlinecode.granite.cluster.node.commons.utils.TargetExistsException;
import com.firstlinecode.granite.cluster.node.commons.utils.ZipUtils;
import com.firstlinecode.granite.cluster.node.mgtnode.Options;

public class AppnodeRuntimesPacker implements IAppnodeRuntimesPacker {
	private static final String DIRECTOFY_NAME_GRANITE_CONFIG = "com.firstlinecode.granite";
	private static final String DIRECTORY_NAME_OSGI_CONFIG = "configuration";
	private static final String DIRECTORY_NAME_PLUGINS = "plugins";
	private static final String DIRECTORY_NAME_PACK_TMP = "pack-tmp";
	private static final String CONFIGURATION_KEY_CONFIGURATOR = "configurator";
	private static final String CONFIGURATION_KEY_BUNDLES = "bundles";
	private static final String CONFIGURATION_KEY_DEPENDED = "depended";
	private static final String NAME_PREFIX_PROTOCOL_MODULE = "protocol-";
	private static final String NAME_PREFIX_ABILITY_MODULE = "ability-";
	private static final String FILE_NAME_PACK_MODULES_CONFIG = "pack_modules.ini";
	private static final String RESOURCE_NAME_PACK_MODULES_CONFIG = "META-INF/com/firstlinecode/granite/pack_modules.ini";
	private static final String NAME_PREFIX_OSGI_BUNDLE = "org.eclipse.osgi-";
	
	private static final Logger logger = LoggerFactory.getLogger(AppnodeRuntimesPacker.class);
	
	private Options options;
	private boolean packModulesLoaded;
	private Map<String, IPackModule> packModules;
	
	public AppnodeRuntimesPacker(Options options) {
		this.options = options;
		packModulesLoaded = false;
		packModules = new HashMap<>();
	}

	@Override
	public void pack(String nodeType, String runtimeName, DeployPlan configuration) {
		File runtimeZip = new File(new File(options.getAppnodeRuntimesDir()), runtimeName + ".zip");
		if (runtimeZip.exists()) {
			if (!options.isRepack()) {
				logger.info("Runtime {} has already existed. Packing is ignored. Use -repack option if you want to repack all runtimes anyway.", runtimeName);
				return;
			}
			
			try {
				logger.info("Runtime {} existed. Deleting it...", runtimeName);
				Files.delete(runtimeZip.toPath());
			} catch (IOException e) {
				throw new RuntimeException(String.format("Can't delete runtime zip file %s.", runtimeZip.getPath()), e);
			}
		}
		
		File packTmpDir = new File(options.getAppnodeRuntimesDir(), DIRECTORY_NAME_PACK_TMP);
		
		try {
			doPack(nodeType, runtimeName, configuration, new File(options.getAppnodeRuntimesDir()), packTmpDir);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Can't pack appnode runtime %s.", runtimeName), e);
		} finally {
			if (packTmpDir != null && packTmpDir.exists()) {
				logger.debug("Removing pack temporary directory...");
				IoUtils.deleteFileRecursively(packTmpDir);
			}
		}
	}

	private void doPack(String nodeType, String runtimeName, DeployPlan configuration,
			File runtimesDir, File packTmpDir) throws IOException {
		if (!isPackModulesLoaded()) {
			logger.debug("Ready to load pack modules.");
			loadPackModules();
			logger.debug("Pack modules loaded.");
		}
		
		if (!packTmpDir.exists()) {
			logger.debug("Pack temporary directory doesn't exist. Creating it...");
			Files.createDirectories(packTmpDir.toPath());
		}
		
		Path runtimeTmpDirPath = createChildDir(packTmpDir.toPath(), runtimeName);
		Path pluginsDirPath = createChildDir(runtimeTmpDirPath, DIRECTORY_NAME_PLUGINS);
		Path osgiConfigDirPath = createChildDir(runtimeTmpDirPath, DIRECTORY_NAME_OSGI_CONFIG);
		Path graniteConfigDirPath = createChildDir(osgiConfigDirPath, DIRECTOFY_NAME_GRANITE_CONFIG);
		
		IPackContext context = createContext(new File(options.getConfigDir()).toPath(),
				new File(options.getRepositoryDir()).toPath(), runtimeTmpDirPath, pluginsDirPath,
					osgiConfigDirPath, graniteConfigDirPath, packModules, nodeType, configuration);
		NodeType node = configuration.getNodeTypes().get(nodeType);
		
		logger.debug("Packing node {}.", nodeType);
		
		copyOsgiJar(context);
		copyBundles(context, node, configuration);
		configure(context, node, configuration);
		
		zipRuntime(packTmpDir, runtimesDir, runtimeName);
	}

	private void copyOsgiJar(IPackContext context) {
		File repositoryDir = context.getRepositoryDir().toFile();
		File osgiBundle = null;
		for (File aBundle : repositoryDir.listFiles()) {
			if (aBundle.getName().startsWith(NAME_PREFIX_OSGI_BUNDLE)) {
				osgiBundle = aBundle;
				break;
			}
		}
		
		if (osgiBundle == null) {
			throw new RuntimeException(String.format("OSGi bundle not found."));
		}
		
		File target = new File(context.getRuntimeDir().toFile(), osgiBundle.getName());
		try {
			Files.copy(osgiBundle.toPath(), target.toPath());
		} catch (IOException e) {
			throw new RuntimeException("Can't copy OSGi bundle to runtime directory.");
		}
	}
	
	private void zipRuntime(File packTmpDir, File runtimesDir, String runtimeName) throws IOException {
		File runtimeZip = new File(runtimesDir, runtimeName + ".zip");
		if (runtimeZip.exists()) {
			Files.delete(runtimeZip.toPath());
		}
		
		try {
			ZipUtils.zip(packTmpDir, runtimeZip);
		} catch (TargetExistsException e) {
			// ??? is it impossible?
			throw new RuntimeException("Runtime zip has alread existed.", e);
		}
	}

	private Path createChildDir(Path parentPath, String name) {
		File childDir = new File(parentPath.toFile(), name);
		
		try {
			return Files.createDirectory(childDir.toPath());
		} catch (IOException e) {
			throw new RuntimeException(String.format("Can't create directory %s",
					childDir.getPath()), e);
		}
	}

	private void configure(IPackContext context, NodeType node, DeployPlan configuration) {
		for (String abilityName : node.getAbilities()) {
			IPackModule module = packModules.get(NAME_PREFIX_ABILITY_MODULE + abilityName);
			module.configure(context, configuration);
		}
		
		for (String protocolName : node.getProtocols()) {
			IPackModule module = packModules.get(NAME_PREFIX_PROTOCOL_MODULE + protocolName);
			module.configure(context, configuration);
		}
		
		context.getConfigManager().saveConfigs();
	}

	private void copyBundles(IPackContext context, NodeType node, DeployPlan configuration) {
		for (String abilityName : node.getAbilities()) {
			logger.info("Copying ability[{}] bundles...", abilityName);
			IPackModule module = packModules.get(NAME_PREFIX_ABILITY_MODULE + abilityName);
			module.copyBundles(context);
		}
		
		if (node.getProtocols() != null) {
			for (String protocolName : node.getProtocols()) {
				logger.info("Copying protocol[{}] bundles...", protocolName);
				IPackModule module = packModules.get(NAME_PREFIX_PROTOCOL_MODULE + protocolName);
				module.copyBundles(context);
			}
		}
	}

	private IPackContext createContext(Path configDir, Path repositoryDirPath, Path runtimeDirPath, Path pluginsDirPath,
			Path osgiConfigDirPath, Path graniteConfigPath, Map<String, IPackModule> packModules, String nodeType,
					DeployPlan configuration) {
		return new PackContext(configDir, repositoryDirPath, runtimeDirPath, pluginsDirPath, osgiConfigDirPath,
				graniteConfigPath, packModules, nodeType, configuration);
	}
	
	private void loadPackModules() {
		InputStream packModulesConfigInputStream = null; 
		
		File packModulesConfigFile = new File(options.getConfigDir(), FILE_NAME_PACK_MODULES_CONFIG);
		if (packModulesConfigFile.exists()) {
			try {
				packModulesConfigInputStream = new FileInputStream(packModulesConfigFile);
			} catch (FileNotFoundException e) {
				// ignore. try load pack modules configuration file from jar resource.
			}
		}
		
		if (packModulesConfigInputStream == null) {
			URL resource = getClass().getClassLoader().getResource(RESOURCE_NAME_PACK_MODULES_CONFIG);
			if (resource == null)
				throw new RuntimeException("Can't get pack_modules.ini.");
			
			try {
				packModulesConfigInputStream = resource.openStream();
			} catch (IOException e) {
				throw new IllegalArgumentException("Can't load pack_modules.ini.", e);
			}
		}
		
		SectionalProperties sp = new SectionalProperties();
		try {
			sp.load(packModulesConfigInputStream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Can't load pack_modules.ini.", e);
		}
		
		for (String sectionName : sp.getSectionNames()) {
			Properties properties = sp.getSection(sectionName);
			
			String[] dependedModules = getDependedModules(StringUtils.stringToArray((String)properties.getProperty(CONFIGURATION_KEY_DEPENDED)));
			CopyBundleOperation[] copyBundles = getCopyBundleOperations(StringUtils.stringToArray(properties.getProperty(CONFIGURATION_KEY_BUNDLES)));
			IPackConfigurator configurator = getConfigurator(properties.getProperty(CONFIGURATION_KEY_CONFIGURATOR));
			
			packModules.put(sectionName, new PackModule(dependedModules, copyBundles, configurator));
		}
		
	}

	private String[] getDependedModules(String[] dependedModules) {
		return (dependedModules == null || dependedModules.length == 0) ? null : dependedModules;
	}

	private IPackConfigurator getConfigurator(String sConfigurator) {
		if (sConfigurator == null)
			return null;
		
		try {
			Class<?> configuratorClass = Class.forName(sConfigurator);
			
			if (!IPackConfigurator.class.isAssignableFrom(configuratorClass)) {
				throw new IllegalArgumentException(String.format("Pack configurator %s must implements interface %s.",
						sConfigurator, IPackConfigurator.class.getName()));
			}
			
			return (IPackConfigurator)configuratorClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't initiate pack configurator %s.", sConfigurator), e);
		}
	}

	private CopyBundleOperation[] getCopyBundleOperations(String[] sCopyBundles) {
		if (sCopyBundles.length == 0)
			return null;
		
		CopyBundleOperation[] copyBundles = new CopyBundleOperation[sCopyBundles.length];
		for (int i = 0; i < sCopyBundles.length; i++) {
			copyBundles[i] = new CopyBundleOperation(sCopyBundles[i]);
		}
		
		return copyBundles;
	}

	private boolean isPackModulesLoaded() {
		return packModulesLoaded == true;
	}

}
