package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.modules;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;
import com.firstlinecode.granite.cluster.node.commons.deploying.Global;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackConfigurator;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.IPackContext;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.ConfigFiles;
import com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack.config.IConfig;

public class ProtocolBxmppConfigurator implements IPackConfigurator  {

	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		IConfig config = context.getConfigManager().createOrGetConfig(context.getRuntimeGraniteConfigDir(), ConfigFiles.GRANITE_APPLICATION_CONFIG_FILE);
		config.addOrUpdateProperty("message.format", Global.MESSAGE_FORMAT_BINARY);
	}
	
}
