package com.firstlinecode.granite.framework.core.platform;

import java.net.URL;

public interface IPlatform {
	URL getHomeDirectory();
	URL getConfigurationDirectory();
}
