package com.firstlinecode.granite.framework.core.repository;

import com.firstlinecode.granite.framework.core.IService;

public interface IServiceWrapper {
	String getId();
	IService create() throws ServiceCreationException;
}
