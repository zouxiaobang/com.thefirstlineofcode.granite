package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.commons.deploying.Global;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackConfigurator;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackContext;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.IConfig;

public class AppClusterConfigurator implements IPackConfigurator {

	private static final String FILE_NAME_CLUSTERING_INI = "clustering.ini";

	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		copyClusteringConfigFile(context);
		configureGlobalParams(context, configuration.getGlobal());
	}

	private void configureGlobalParams(IPackContext context, Global global) {
		IConfig clusterConfig = context.getConfigManager().createOrGetConfig(context.getRuntimeGraniteConfigDir(), FILE_NAME_CLUSTERING_INI);
		IConfig sessionStorageConfig = clusterConfig.getSection("session-storage");
		sessionStorageConfig.addOrUpdateProperty("session-duration-time", Integer.toString(global.getSessionDurationTime()));
	}

	private void copyClusteringConfigFile(IPackContext context) {
		File nodeTypeClusteringConfigFile = new File(context.getConfigDir().toFile(), context.getNodeType() + "-clustering.ini");
		if (copyClusteringConfigFile(context, nodeTypeClusteringConfigFile))
			return;
		
		File defaultClusteringConfigFile = new File(context.getConfigDir().toFile(), "default-clustering.ini");
		if (!copyClusteringConfigFile(context, defaultClusteringConfigFile)) {
			throw new RuntimeException(String.format("Can't find a clustering config file to copy. You should put %s or %s to mgtnode configuration directory.",
					defaultClusteringConfigFile.getName(), nodeTypeClusteringConfigFile.getName()));
		}
	}
	
	private boolean copyClusteringConfigFile(IPackContext context, File sourceFile) {
		if (sourceFile.exists()) {
			try {
				Files.copy(sourceFile.toPath(), new File(context.getRuntimeGraniteConfigDir().toFile(),
						FILE_NAME_CLUSTERING_INI).toPath());
				
				return true;
			} catch (IOException e) {
				throw new RuntimeException("Failed to copy clustering config file.", e);
			}
		}
		
		return false;
	}

}
