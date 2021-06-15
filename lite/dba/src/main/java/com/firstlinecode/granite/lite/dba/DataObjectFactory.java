package com.firstlinecode.granite.lite.dba;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.firstlinecode.granite.framework.adf.mybatis.DataObjectMapping;
import com.firstlinecode.granite.framework.adf.mybatis.IDataObjectsContributor;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.adf.data.IDataObjectFactory;
import com.firstlinecode.granite.framework.core.adf.data.IIdProvider;
import com.firstlinecode.granite.framework.core.annotations.AppComponent;
import com.firstlinecode.granite.framework.core.repository.IInitializable;

@AppComponent("data.object.factory")
public class DataObjectFactory implements IDataObjectFactory, IInitializable, IApplicationComponentServiceAware {	
	private IApplicationComponentService appComponentService;
	private Map<Class<?>, Class<?>> dataObjectMappings;
	private volatile boolean inited;
	
	public DataObjectFactory() {
		inited = false;
		dataObjectMappings = new HashMap<>();
	}
	
	@Override
	public void init() {
		if (inited)
			return;
		
		synchronized (this) {
			if (inited)
				return;
			
			List<IDataObjectsContributor> dataObjectsContributors = appComponentService.getPluginManager().
					getExtensions(IDataObjectsContributor.class);
			for (IDataObjectsContributor dataObjectsContributor : dataObjectsContributors) {
				DataObjectMapping<?>[] mappings = dataObjectsContributor.getDataObjectMappings();
				if (mappings == null || mappings.length == 0)
					continue;
				
				for (DataObjectMapping<?> mapping : mappings) {
					if (dataObjectMappings.containsKey(mapping.domainType))
						throw new IllegalArgumentException(String.format("Reduplicated domain data object type: '%s'.", mapping.domainType));
					
					dataObjectMappings.put(mapping.domainType, mapping.dataType);
				}
			}
			
			inited = true;
		}
	}
		
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <K, V extends K> V create(Class<K> clazz) {
		if (!inited)
			init();
		
		try {
			V object = doCreate(clazz);
			
			if (object instanceof IIdProvider) {
				((IIdProvider)object).setId(UUID.randomUUID().toString());
			}
			
			return object;
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't create data object for class '%s'.", clazz.getName()), e);
		}
	}

	@SuppressWarnings("unchecked")
	private <K, V extends K> V doCreate(Class<K> domainType) throws InstantiationException, IllegalAccessException {
		Class<?> dataType = dataObjectMappings.get(domainType);
		if (dataType == null) {
			return (V)domainType.newInstance();
		}
		
		return (V)dataType.newInstance();
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

}
