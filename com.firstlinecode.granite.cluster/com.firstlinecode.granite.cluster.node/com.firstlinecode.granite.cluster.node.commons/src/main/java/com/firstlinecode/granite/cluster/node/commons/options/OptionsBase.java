package com.firstlinecode.granite.cluster.node.commons.options;

public class OptionsBase {
	private boolean help;
	private String homeDir;
	private String configDir;
	
	public void setHelp(boolean help) {
		this.help = help;
	}
	
	public boolean isHelp() {
		return help;
	}
	
	public String getHomeDir() {
		return homeDir;
	}

	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}

	public String getConfigDir() {
		return configDir;
	}

	public void setConfigDir(String configDir) {
		this.configDir = configDir;
	}

}
