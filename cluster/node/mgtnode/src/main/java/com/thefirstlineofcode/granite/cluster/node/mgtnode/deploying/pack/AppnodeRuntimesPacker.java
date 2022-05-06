package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack;

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

import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlan;
import com.thefirstlineofcode.granite.cluster.node.commons.deploying.NodeType;
import com.thefirstlineofcode.granite.cluster.node.commons.utils.IoUtils;
import com.thefirstlineofcode.granite.cluster.node.commons.utils.SectionalProperties;
import com.thefirstlineofcode.granite.cluster.node.commons.utils.StringUtils;
import com.thefirstlineofcode.granite.cluster.node.commons.utils.TargetExistsException;
import com.thefirstlineofcode.granite.cluster.node.commons.utils.ZipUtils;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.Options;

public class AppnodeRuntimesPacker implements IAppnodeRuntimesPacker {
	private static final String DIRECTORY_NAME_LIBS = "libs";
	private static final String DIRECTORY_NAME_PLUGINS = "plugins";
	private static final String DIRECTORY_NAME_CLUSTER = "cluster";
	private static final String DIRECTORY_NAME_PACK_TMP = "pack-tmp";
	private static final String CONFIGURATION_KEY_CONFIGURATOR = "configurator";
	private static final String CONFIGURATION_KEY_LIBRARIES = "libraries";
	private static final String CONFIGURATION_KEY_DEPENDED = "depended";
	private static final String NAME_PREFIX_PROTOCOL_MODULE = "protocol-";
	private static final String NAME_PREFIX_ABILITY_MODULE = "ability-";
	private static final String FILE_NAME_PACK_MODULES_CONFIG = "pack_modules.ini";
	private static final String RESOURCE_NAME_PACK_MODULES_CONFIG = "META-INF/com/thefirstlineofcode/granite/pack_modules.ini";
	
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
		Path libsDirPath = createChildDir(runtimeTmpDirPath, DIRECTORY_NAME_LIBS);
		Path pluginsDirPath = createChildDir(runtimeTmpDirPath, DIRECTORY_NAME_PLUGINS);
		
		IPackContext context = createContext(
				new File(options.getConfigurationDir()).toPath(),
				new File(options.getConfigurationDir(), DIRECTORY_NAME_CLUSTER).toPath(),
				new File(options.getRepositoryDir()).toPath(),
				runtimeTmpDirPath, libsDirPath, pluginsDirPath, packModules, nodeType, configuration);
		NodeType node = configuration.getNodeTypes().get(nodeType);
		
		logger.debug("Packing node {}.", nodeType);
		
		copyPackModules(context, node, configuration);
		configure(context, node, configuration);
		
		zipRuntime(packTmpDir, runtimesDir, runtimeName);
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

	private void copyPackModules(IPackContext context, NodeType node, DeployPlan configuration) {
		for (String abilityName : node.getAbilities()) {
			logger.info("Copying ability[{}] libraries...", abilityName);
			IPackModule module = packModules.get(NAME_PREFIX_ABILITY_MODULE + abilityName);
			module.copyLibraries(context);
		}
		
		if (node.getProtocols() != null) {
			for (String protocolName : node.getProtocols()) {
				logger.info("Copying protocol[{}] libraries...", protocolName);
				IPackModule module = packModules.get(NAME_PREFIX_PROTOCOL_MODULE + protocolName);
				module.copyLibraries(context);
			}
		}
	}

	private IPackContext createContext(Path configDir, Path clusterConfigurationDir,
			Path repositoryDirPath, Path runtimeDirPath, Path libsDirPath, Path pluginsDirPath,
			Map<String, IPackModule> packModules, String nodeType,
					DeployPlan configuration) {
		return new PackContext(configDir, clusterConfigurationDir, repositoryDirPath, runtimeDirPath,
				libsDirPath, pluginsDirPath, packModules, nodeType, configuration);
	}
	
	private void loadPackModules() {
		InputStream packModulesConfigInputStream = null; 
		
		File packModulesConfigFile = new File(options.getConfigurationDir(), FILE_NAME_PACK_MODULES_CONFIG);
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
			CopyLibraryOperation[] copyLibraries = getCopyLibraryOperations(StringUtils.stringToArray(properties.getProperty(CONFIGURATION_KEY_LIBRARIES)));
			IPackConfigurator configurator = getConfigurator(properties.getProperty(CONFIGURATION_KEY_CONFIGURATOR));
			
			packModules.put(sectionName, new PackModule(dependedModules, copyLibraries, configurator));
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

	private CopyLibraryOperation[] getCopyLibraryOperations(String[] sCopyLibraries) {
		if (sCopyLibraries.length == 0)
			return null;
		
		CopyLibraryOperation[] copyLibraries = new CopyLibraryOperation[sCopyLibraries.length];
		for (int i = 0; i < sCopyLibraries.length; i++) {
			String libraryName = sCopyLibraries[i];
			boolean optional = false;
			int optionalSeparator = sCopyLibraries[i].indexOf(" - ");
			if (optionalSeparator != -1) {
				libraryName = sCopyLibraries[i].substring(0, optionalSeparator).trim();
				String sOptional = sCopyLibraries[i].substring(optionalSeparator + 3).trim();
				if (sOptional == null || sOptional.isEmpty()) {						
					throw new RuntimeException("Illegal pack module configuration format. Check pack_modules.ini file.");
				}
				
				optional = sOptional.equals("optional");
			}
			
			copyLibraries[i] = new CopyLibraryOperation(libraryName, optional);
		}
		
		return copyLibraries;
	}

	private boolean isPackModulesLoaded() {
		return packModulesLoaded == true;
	}

}
