package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack;

import com.firstlinecode.granite.cluster.node.commons.deploying.DeployPlan;

public class PackModule implements IPackModule {
	private String[] dependedModules;
	private CopyBundleOperation[] copyBundles;
	private IPackConfigurator configurator;
	private boolean bundlesCopied;
	private boolean configured;
	
	public PackModule(String[] dependedModules, CopyBundleOperation[] copyBundles,
			IPackConfigurator configurator) {
		this.dependedModules = dependedModules;
		this.copyBundles = copyBundles;
		this.configurator = configurator;
		
		bundlesCopied = false;
		configured = false;
	}
	
	@Override
	public void copyBundles(IPackContext context) {
		if (dependedModules != null) {
			for (String sModule : dependedModules) {
				IPackModule module = context.getPackModule(sModule);
				if (!module.isBundlesCopied()) {
					module.copyBundles(context);
				}
			}
		}
		
		if (copyBundles != null) {
			for (CopyBundleOperation operation : copyBundles) {
				operation.copy(context);
			}
		}
		
		bundlesCopied = true;
	}
	
	@Override
	public void configure(IPackContext context, DeployPlan configuration) {
		if (dependedModules != null) {
			for (String sModule : dependedModules) {
				IPackModule module = context.getPackModule(sModule);
				if (!module.isConfigured()) {
					module.configure(context, configuration);
				}
			}
		}
		
		if (configurator != null) {
			configurator.configure(context, configuration);
		}
		
		configured = true;
	}
	
	@Override
	public boolean isBundlesCopied() {
		return bundlesCopied;
	}
	
	@Override
	public boolean isConfigured() {
		return configured;
	}
	
	@Override
	public String[] getDependedModules() {
		return dependedModules;
	}
	
	@Override
	public CopyBundleOperation[] getCopyBundles() {
		return copyBundles;
	}
	
	@Override
	public IPackConfigurator getCallback() {
		return configurator;
	}
}
