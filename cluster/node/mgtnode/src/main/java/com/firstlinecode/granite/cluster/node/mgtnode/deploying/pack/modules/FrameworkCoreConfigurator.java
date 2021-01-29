package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.modules;

import java.io.File;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.commons.deploying.Global;
import com.firstlinecode.granite.cluster.node.commons.utils.StringUtils;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackConfigurator;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackContext;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.ConfigFiles;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.IConfig;

public class FrameworkCoreConfigurator implements IPackConfigurator {
	private static final String NAME_PREFIX_OSGI_BUNDLE = "org.eclipse.osgi-";
	private static final String NAME_PREFIX_ECLIPSE_COMMON_BUNDLE = "org.eclipse.equinox.common-";
	private static final String NAME_PREFIX_ECLIPSE_UPDATE_BUNDLE = "org.eclipse.update.configurator-";
	
	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		configureOsgiConfigIni(context);
		configureApplicationIni(context);
		configureComponentBindingIni(context);
		//configureGlobalFeatureParams(context);
	}

	/*private void configureGlobalFeatureParams(IPackContext context) {
		IConfig config = context.getConfigManager().createOrGetConfig(context.getRuntimeGraniteConfigDir(), ConfigFiles.GRANITE_COMPONENTS_CONFIG_FILE);
		Properties globalParams = context.getDeployConfiguration().getNodeTypes().get(
				context.getNodeType()).getConfiguration("*");
		
		String sessionCallbackCheckInterval = globalParams.getProperty("session-callback-check-interval");
		if (sessionCallbackCheckInterval != null) {
			try {
				Integer.parseInt(sessionCallbackCheckInterval);
			} catch (Exception e) {
				throw new IllegalArgumentException("Global feature parameter 'session-callback-check-interval' must be an integer.");
			}
			
			IConfig sessionManagerConfig = config.getSection("session.manager");
			sessionManagerConfig.addOrUpdateProperty("session.callback.check.interval", sessionCallbackCheckInterval);
		}
	}*/

	private void configureComponentBindingIni(IPackContext context) {
		// Just create an empty component binding configuration file.
		context.getConfigManager().createOrGetConfig(context.getRuntimeGraniteConfigDir(), ConfigFiles.GRANITE_COMPONENT_BINDING_CONFIG_FILE);
	}

	private void configureApplicationIni(IPackContext context) {
		IConfig config = context.getConfigManager().createOrGetConfig(context.getRuntimeGraniteConfigDir(), ConfigFiles.GRANITE_APPLICATION_CONFIG_FILE);
		
		config.addOrUpdateProperty("domain.name", context.getDeployConfiguration().getCluster().getDomainName());
		
		String[] domainAliasNames = context.getDeployConfiguration().getCluster().getDomainAliasNames();
		if (domainAliasNames != null && domainAliasNames.length != 0) {
			config.addOrUpdateProperty("domain.alias.names", StringUtils.arrayToString(domainAliasNames));
		}
		
		config.addOrUpdateProperty("component.binding.profile", "${config.dir}/" + ConfigFiles.GRANITE_COMPONENT_BINDING_CONFIG_FILE);
		
		config.addComment("You can uncomment the line below to disable some services.\r\ndisabled.services=stream.service");
		
		String messageFormat = context.getDeployConfiguration().getGlobal().getMessageFormat();
		if (Global.MESSAGE_FORMAT_BINARY.equals(messageFormat)) {
			config.addOrUpdateProperty("message-format", messageFormat);
		}
	}

	private void configureOsgiConfigIni(IPackContext context) {
		File[] plugins = context.getRuntimePluginsDir().toFile().listFiles();
		String configIniContent = generateOsgiConfigIniContent(plugins);
		
		IConfig config = context.getConfigManager().createOrGetConfig(context.getRuntimeOsgiConfigDir(), "config.ini");
		config.setContent(configIniContent);
	}
	
	private String generateOsgiConfigIniContent(File[] bundles) {
		String eclipseCommonBundleName = null;
		String eclipseUpdateBundleName = null;
		StringBuilder bundlesReference = new StringBuilder();
		for (File bundle : bundles) {
			if (eclipseCommonBundleName == null && bundle.getName().startsWith(NAME_PREFIX_ECLIPSE_COMMON_BUNDLE)) {
				eclipseCommonBundleName = bundle.getName();
			} else if (eclipseUpdateBundleName == null && bundle.getName().startsWith(NAME_PREFIX_ECLIPSE_UPDATE_BUNDLE)) {
				eclipseUpdateBundleName = bundle.getName();
			} else if (bundle.getName().startsWith(NAME_PREFIX_OSGI_BUNDLE)) {
				continue;
			} else {
				bundlesReference.
					append(",\\").
					append("\r\n").
					append("reference:file:plugins/").
					append(bundle.getName()).
					append("@start");
			}
		}
		
		if (eclipseCommonBundleName == null || eclipseUpdateBundleName == null) {
			throw new RuntimeException("Eclipse common bundle or update bundle could not be found.");
		}
		
		StringBuilder content = new StringBuilder();
		content.
			append("osgi.bundles=\\").
			append("\r\n").
			append("reference:file:plugins/").
			append(eclipseCommonBundleName).append("@2:start").
			append(",\\").
			append("\r\n").
			append("reference:file:plugins/").
			append(eclipseUpdateBundleName).append("@3:start").
			append(bundlesReference.toString());
		
		content.
			append("\r\n").
			append("eclipse.ignoreApp=true");
		
		content.
			append("\r\n").
			append("osgi.bundles.defaultStartLevel=4");
		
		// XPathFactory uses com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl(in Oracle JDK) as default XPathFactory implementation
/*		content.
			append("\r\n").
			append("org.osgi.framework.bootdelegation=").
			append("com.sun.org.apache.xpath.internal.jaxp");*/
		content.
			append("\r\n").
			append("org.osgi.framework.bootdelegation=*");
		content.
			append("\r\n").
			append("osgi.compatibility.bootdelegation=true");
		
		return content.toString();
	}
}
