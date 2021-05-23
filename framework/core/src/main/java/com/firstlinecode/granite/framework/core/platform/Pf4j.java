package com.firstlinecode.granite.framework.core.platform;

import java.io.File;

public class Pf4j implements IPlatform {

	@Override
	public String getHomeDirectory() {
		return System.getProperty("user.dir");
	}

	@Override
	public String getConfigurationDirectory() {
		return new File(getHomeDirectory(), "configuration").getAbsolutePath();
	}

}
