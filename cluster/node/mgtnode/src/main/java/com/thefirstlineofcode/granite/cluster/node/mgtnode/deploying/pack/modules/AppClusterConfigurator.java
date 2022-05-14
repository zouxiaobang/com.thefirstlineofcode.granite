package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlan;
import com.thefirstlineofcode.granite.cluster.node.commons.deploying.Global;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.IPackConfigurator;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.IPackContext;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.config.IConfig;

public class AppClusterConfigurator implements IPackConfigurator {
	private static final String FILE_NAME_CLUSTERING_INI = "clustering.ini";

	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		copyClusteringConfigFile(context);
		configureGlobalParams(context, configuration.getGlobal());
		configureJavaUtilLogging(context);
	}

	private void configureJavaUtilLogging(IPackContext context) {
		IConfig javaUtilLoggingConfig = context.getConfigManager().createOrGetConfig(
				context.getRuntimeConfigurationDir().toPath(), "java_util_logging.ini");
		javaUtilLoggingConfig.addOrUpdateProperty("handlers", "java.util.logging.FileHandler, java.util.logging.ConsoleHandler");
		javaUtilLoggingConfig.addOrUpdateProperty(".level", "INFO");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.pattern", "%h/appnode_rt.log%g.log");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.limit", "50000");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.count", "5");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.formatter", "java.util.logging.SimpleFormatter");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.ConsoleHandler.level", "SEVERE");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.ConsoleHandler.formatter", "java.util.logging.SimpleFormatter");
	}

	private void configureGlobalParams(IPackContext context, Global global) {
		IConfig clusterConfig = context.getConfigManager().createOrGetConfig(
				context.getRuntimeConfigurationDir().toPath(), FILE_NAME_CLUSTERING_INI);
		IConfig sessionStorageConfig = clusterConfig.getSection("session-storage");
		sessionStorageConfig.addOrUpdateProperty("session-duration-time", Integer.toString(global.getSessionDurationTime()));
	}

	private void copyClusteringConfigFile(IPackContext context) {
		Path nodeTypeClusteringConfigFilePath = context.getConfigurationDir().toPath().
				resolve(context.getNodeType() + "-clustering.ini");
		if (copyClusteringConfigFile(context, nodeTypeClusteringConfigFilePath))
			return;
		
		Path defaultClusteringConfigFilePath = context.getConfigurationDir().toPath().resolve("default-clustering.ini");
		if (!copyClusteringConfigFile(context, defaultClusteringConfigFilePath)) {
			throw new RuntimeException(String.format("Can't find a clustering config file to copy. You should put %s or %s to mgtnode cluster configuration directory.",
					defaultClusteringConfigFilePath, defaultClusteringConfigFilePath));
		}
	}
	
	private boolean copyClusteringConfigFile(IPackContext context, Path sourceFilePath) {
		if (sourceFilePath.toFile().exists()) {
			try {
				Files.copy(sourceFilePath, getClusteringIniFileTargetPath(context.getRuntimeConfigurationDir().toPath()));
				
				return true;
			} catch (IOException e) {
				throw new RuntimeException("Failed to copy clustering config file.", e);
			}
		}
		
		return false;
	}

	private Path getClusteringIniFileTargetPath(Path runtimeConfigurationDirPath) {
		return runtimeConfigurationDirPath.resolve(FILE_NAME_CLUSTERING_INI);
	}
}
