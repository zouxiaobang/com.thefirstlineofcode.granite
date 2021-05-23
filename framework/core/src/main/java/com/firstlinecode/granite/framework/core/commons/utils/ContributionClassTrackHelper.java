package com.firstlinecode.granite.framework.core.commons.utils;

import java.lang.annotation.Annotation;

public class ContributionClassTrackHelper<T extends Annotation> /*implements IContributionTracker*/ {
/*	private static final String VALUE_ENABLE_CONTRIBUTION_SCAN = "true";
	private static final char SEPARATOR_CLASS_NAME = '.';
	private static final char SEPARATOR_PATH = '/';
	private static final String SEPARATOR_PARAMETER_VALUE = ";";
	private static final String PATH_ROOT = "/";
	
	private BundleContext bundleContext;
	private String contributionKey;
	private String contributionScanPathsKey;
	private Class<T> annotation;
	private IContributionClassTracker<T> tracker;
	
	private Map<String, List<ContributionClass<T>>> bundleToContributionClasses;
	
	public ContributionClassTrackHelper(BundleContext bundleContext, String contributionKey,
			String contributionScanPathsKey, Class<T> annotation,
				IContributionClassTracker<T> tracker) {
		this.bundleContext = bundleContext;
		this.contributionKey = contributionKey;
		this.contributionScanPathsKey = contributionScanPathsKey;
		this.annotation = annotation;
		this.tracker = tracker;
		
		bundleToContributionClasses = new HashMap<>();
	}
	
	public void track() {
		OsgiUtils.trackContribution(bundleContext, contributionKey, this);
	}
	
	public void stopTrack() {
		OsgiUtils.stopTrackContribution(bundleContext, this);
	}

	@Override
	public void found(Bundle bundle, String contribution) throws Exception {
		if (!VALUE_ENABLE_CONTRIBUTION_SCAN.equals(contribution)) {
			return;
		}
		
		String sScanPaths = bundle.getHeaders().get(contributionScanPathsKey);
		
		if (sScanPaths == null) {
			scanPaths(bundle, new String[] {PATH_ROOT});
		} else {
			scanPaths(bundle, getScanPaths(sScanPaths));
		}
	}
	
	private String[] getScanPaths(String scanPathsString) {
		StringTokenizer tokenizer = new StringTokenizer(scanPathsString, SEPARATOR_PARAMETER_VALUE);
		
		String[] paths = new String[tokenizer.countTokens()];
		for (int i = 0; i< paths.length; i++) {
			if (tokenizer.hasMoreTokens()) {
				paths[i] = tokenizer.nextToken();
			}
		}
		
		return paths;
	}
	
	private void scanPaths(Bundle bundle, String[] paths) {
		List<ContributionClass<T>> contributionClasses = new ArrayList<>();
		
		for (String path : paths) {
			Enumeration<URL> urls = bundle.findEntries(path, "*.class", true);
			
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				
				String filePath = url.getFile();
				if (filePath.startsWith(PATH_ROOT)) {
					filePath = filePath.substring(1);
				}
				
				String className = filePath.replace(SEPARATOR_PATH, SEPARATOR_CLASS_NAME);
				className = className.substring(0, className.length() - 6);
				try {
					Class<?> clazz = bundle.loadClass(className);
					
					if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation())
						continue;
					
					T contributionAnnotation = clazz.getAnnotation(annotation);
					if (contributionAnnotation != null) {
						ContributionClass<T> cc = new ContributionClass<>(bundle.getPluginWrapper(), clazz, contributionAnnotation);
						cc.setAnnotation(contributionAnnotation);
						cc.setType(clazz);
						
						contributionClasses.add(cc);
					}
				} catch (Exception e) {
					throw new RuntimeException(String.format("Can't load class %s when scaning contribution.",
							className), e);
				}	
			}
		}
		
		bundleToContributionClasses.put(bundle.getSymbolicName(), contributionClasses);
		
		for (ContributionClass<T> cc : contributionClasses) {
			tracker.found(cc);
		}
	}

	@Override
	public void lost(Bundle bundle, String contribution) throws Exception {
		List<ContributionClass<T>> contributionClasses = bundleToContributionClasses.remove(bundle.getSymbolicName());
		
		if (contributionClasses == null || contributionClasses.size() == 0)
			return;
		
		for (ContributionClass<T> cc : contributionClasses) {
			tracker.lost(cc);
		}
	}*/
}
