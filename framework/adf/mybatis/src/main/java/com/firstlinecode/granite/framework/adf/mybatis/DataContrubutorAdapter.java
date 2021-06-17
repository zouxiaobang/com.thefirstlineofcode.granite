package com.firstlinecode.granite.framework.adf.mybatis;

import java.net.URL;

public abstract class DataContrubutorAdapter implements IDataContributor {

	private static final String DIRECTORY_OF_MAPPER_RESOURCES = "META-INF/mybatis/";

	@Override
	public TypeHandlerMapping<?>[] getTypeHandlerMappings() {
		return null;
	}
	
	@Override
	public URL[] getMappers() {
		String[] mapperFileNames = getMapperFileNames();
		if (mapperFileNames == null || mapperFileNames.length == 0)
			return null;;
		
		URL[] urls = new URL[mapperFileNames.length];
		for (int i = 0; i < mapperFileNames.length; i++) {
			String resourceName = DIRECTORY_OF_MAPPER_RESOURCES + mapperFileNames[i];
			urls[i] = getClass().getClassLoader().getResource(resourceName);
		}
		
		return urls;
	}

	protected String[] getMapperFileNames() {
		return null;
	}
}
