package com.firstlinecode.granite.lite.dba.internal;

public class DataObjectFactory /*implements IDataObjectFactory*/ {
	/*private static final String KEY_GRANITE_PERSISTENT_OBJECTS = "Granite-MyBatis-Data-Objects";
	
	private Map<Class<?>, DataObjectMapping> objectMappings;
	private IContributionTracker tracker;
	
	public DataObjectFactory(BundleContext bundleContext) {
		objectMappings = new ConcurrentHashMap<>();
		tracker = new PersistentObjectsTracker();
		
		OsgiUtils.trackContribution(bundleContext, KEY_GRANITE_PERSISTENT_OBJECTS, tracker);
	}
	
	private class DataObjectMapping {
		public Class<?> domain;
		public Class<?> data;
		public String bundleSymlicName;
		
		public DataObjectMapping(Class<?> domain, Class<?> data, String bundleSymlicName) {
			this.domain = domain;
			this.data = data;
			this.bundleSymlicName = bundleSymlicName;
		}
	}
	
	private class PersistentObjectsTracker implements IContributionTracker {

		@Override
		public void found(Bundle bundle, String contribution) throws Exception {
			String symbolicName = bundle.getSymbolicName();
			StringTokenizer st = new StringTokenizer(contribution, ",");
			while (st.hasMoreTokens()) {
				String sDataConfig = st.nextToken();
				int equalMarkIndex = sDataConfig.indexOf('=');
				String sDomain = null;
				String sData = null;
				if (equalMarkIndex == -1) {
					sData = sDataConfig;
				} else {
					sDomain = sDataConfig.substring(0, equalMarkIndex);
					sData = sDataConfig.substring(equalMarkIndex + 1);
				}
				
				Class<?> data = bundle.loadClass(sData);
				
				Class<?> domain = null;
				if (sDomain != null) {
					domain = bundle.loadClass(sDomain);
				} else {
					domain = data.getSuperclass();
					
					if (domain.equals(Object.class)) {
						continue;
					}
				}
				
				DataObjectMapping mapping = new DataObjectMapping(domain, data, symbolicName);
				objectMappings.put(mapping.domain, mapping);
			}
		}

		@Override
		public void lost(Bundle bundle, String contribution) throws Exception {
			String symbolicName = bundle.getSymbolicName();
			for (DataObjectMapping objectMapping : objectMappings.values()) {
				if (objectMapping.bundleSymlicName.equals(symbolicName))
					objectMappings.remove(objectMapping.domain);
			}
		}
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <K, V extends K> V create(Class<K> clazz) {
		try {
			V object = doCreate(clazz);
			
			if (object instanceof IIdProvider) {
				((IIdProvider)object).setId(UUID.randomUUID().toString());
			}
			
			return object;
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can't create data object for class %s.", clazz.getName()), e);
		}
	}

	@SuppressWarnings("unchecked")
	private <K, V extends K> V doCreate(Class<K> clazz) throws InstantiationException, IllegalAccessException {
		DataObjectMapping objectMapping = objectMappings.get(clazz);
		if (objectMapping == null) {
			return (V)clazz.newInstance();
		}
		
		return (V)objectMapping.data.newInstance();
	} */

}
