package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.modules;

import java.util.List;

import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DbAddress;
import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlan;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.IPackConfigurator;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.IPackContext;
import com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack.config.IConfig;

public class AppDbaConfigurator implements IPackConfigurator {

	private static final String FILE_NAME_DB_INI = "db.ini";

	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		IConfig dbConfig = context.getConfigManager().createOrGetConfig(context.getClusterConfigurationDir(), FILE_NAME_DB_INI);
		dbConfig.addOrUpdateProperty("addresses", getDbAddressesString(configuration.getDb().getAddresses()));
		dbConfig.addOrUpdateProperty("db.name", configuration.getDb().getDbName());
		dbConfig.addOrUpdateProperty("user.name", configuration.getDb().getUserName());
		dbConfig.addOrUpdateProperty("password", new String(configuration.getDb().getPassword()));
		
		IConfig javaUtilLoggingConfig = context.getConfigManager().createOrGetConfig(context.getClusterConfigurationDir(), "java_util_logging.ini");
		javaUtilLoggingConfig.addOrUpdateProperty("handlers", "java.util.logging.FileHandler, java.util.logging.ConsoleHandler");
		javaUtilLoggingConfig.addOrUpdateProperty(".level", "INFO");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.pattern", "%h/appnode_rt.log%g.log");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.limit", "50000");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.count", "5");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.FileHandler.formatter", "java.util.logging.SimpleFormatter");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.ConsoleHandler.level", "SEVERE");
		javaUtilLoggingConfig.addOrUpdateProperty("java.util.logging.ConsoleHandler.formatter", "java.util.logging.SimpleFormatter");
	}

	private String getDbAddressesString(List<DbAddress> addresses) {
		StringBuilder sb = new StringBuilder();
		
		for (DbAddress address : addresses) {
			sb.append(address.getHost()).append(':').append(address.getPort()).append(',');
		}
		
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		
		return sb.toString();
	}

}
