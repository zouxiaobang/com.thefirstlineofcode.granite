package com.firstlinecode.granite.framework.core.integration;

import java.util.List;

public interface IApplicationComponentService {
	void start();
	void stop();
	<T> List<Class<? extends T>> getExtensionClasses(Class<T> type);
	<T> T createExtension(Class<T> type);
	<T> T inject(T rawInstance);
}
