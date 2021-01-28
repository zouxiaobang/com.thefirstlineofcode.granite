package com.firstlinecode.granite.pack.lite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class Updater {
	private static final String GRANITE_SUBSYSTEM_PREFIX = "granite.";
	private static final String SAND_SUBSYSTEM_PREFIX = "sand.";
	
	private static final String GRANITE_PROJECT_PACKAGE_PREFIX = "com.firstlinecode.granite.";

	private static final String DIRECTORY_NAME_CACHE = ".cache";
	private static final String FILE_NAME_SUBSYSTEMS = "subsystems.ini";
	private static final String FILE_NAME_BUNDLEINFOS = "bundleinfos.ini";

	private static final String DIRECTORY_NAME_DOT_GIT = ".git";
	
	private static final String[] GRANITE_SUBSYSTEM_NAMES = new String[] {
			"granite.framework",
			"granite.im",
			"granite.stream",
			"granite.xeps",
			"granite.leps",
			"granite.lite"
	};
	
	private static final String[] SAND_SUBSYSTEM_NAMES = new String[] {
			"sand.protocols",
			"sand.server"
	};
	
	private Map<String, String[]> subsystems;
	private Map<String, BundleInfo> bundleInfos;
	
	private Options options;
	
	public Updater(Options options) {
		this.options = options;
		subsystems = new HashMap<>(8);
		bundleInfos = new HashMap<>(20);
	}

	public void cleanCache() {
		File cacheDir = new File(options.getTargetDirPath(), DIRECTORY_NAME_CACHE);
		if (cacheDir.exists()) {
			new File(cacheDir, FILE_NAME_BUNDLEINFOS).delete();
			new File(cacheDir, FILE_NAME_SUBSYSTEMS).delete();
			cacheDir.delete();
		}
	}
	
	public void update(boolean clean) {
		loadCache();
		
		String[] modules = options.getModules();
		if (modules == null)
			modules = getSubsystems();
		
		List<String> updatedBundles = new ArrayList<>();
		for (String module : modules) {
			if (isSubsystem(module)) {
				updateSubsystem(module, clean, updatedBundles);
			} else {
				if (!module.startsWith(GRANITE_PROJECT_PACKAGE_PREFIX) && !module.startsWith(options.getSandProjectName())) {
					if (module.startsWith(GRANITE_SUBSYSTEM_PREFIX)) {
						module = GRANITE_PROJECT_PACKAGE_PREFIX + module.substring(8);						
					} else if (module.startsWith(SAND_SUBSYSTEM_PREFIX)) {
						module = options.getSandProjectName() + '.' + module.substring(5);
					} else {
						throw new IllegalArgumentException(String.format("Illegal module name '%s'", module));
					}
				}
				
				if (bundleInfos.containsKey(module)) {
					updateBundle(module, clean, updatedBundles);
				} else {
					System.out.println(String.format("Illegal bundle or subsystem: %s.", module));
					return;
				}
			}
		}
		
		StringBuilder bundles = new StringBuilder();
		for (String bundle : updatedBundles) {
			bundles.append(bundle).append(", ");
		}
		
		if (bundles.length() > 0) {
			bundles.delete(bundles.length() - 2, bundles.length());
		}
		
		System.out.println(String.format("Bundles %s updated.", bundles.toString()));
	}

	private String[] getSubsystems() {
		if (options.getSandProjectDirPath() == null) {			
			return GRANITE_SUBSYSTEM_NAMES;
		} else {
			String[] graniteAndSandSubsystemNames = new String[GRANITE_SUBSYSTEM_NAMES.length + SAND_SUBSYSTEM_NAMES.length];
			
			for (int i = 0; i < GRANITE_SUBSYSTEM_NAMES.length; i++) {
				graniteAndSandSubsystemNames[i] = GRANITE_SUBSYSTEM_NAMES[i];
			}
			
			for (int i = 0; i < SAND_SUBSYSTEM_NAMES.length; i++) {
				graniteAndSandSubsystemNames[i + GRANITE_SUBSYSTEM_NAMES.length] = SAND_SUBSYSTEM_NAMES[i];
			}
			
			return graniteAndSandSubsystemNames;
		}
	}
	
	private void updateBundle(String bundle, boolean clean, List<String> updatedBundles) {
		if (bundle.startsWith(GRANITE_SUBSYSTEM_PREFIX)) {
			bundle = GRANITE_PROJECT_PACKAGE_PREFIX + bundle.substring(8);
		} else if (bundle.startsWith(SAND_SUBSYSTEM_PREFIX)) {
			bundle = options.getSandProjectName() + '.' + bundle.substring(5);			
		}
		
		if (!bundleInfos.containsKey(bundle)) {
			throw new IllegalArgumentException(String.format("Illegal bundle name '%s'", bundle));
		}
		
		BundleInfo bundleInfo = bundleInfos.get(bundle);
		if (clean) {
			Main.runMvn(new File(bundleInfo.projectDirPath), options.isOffline(), "clean", "package");
		} else {
			Main.runMvn(new File(bundleInfo.projectDirPath), options.isOffline(), "package");
		}
		
		updateBundle(bundleInfo);
		updatedBundles.add(bundleInfo.fileName);
	}

	private void updateBundle(BundleInfo bundleInfo) {
		File targetDir = new File(bundleInfo.projectDirPath, "target");
		File artifact = new File(targetDir, bundleInfo.fileName);
		if (!artifact.exists()) {
			throw new RuntimeException(String.format("Artifact %s doesn't exist.", artifact.getPath()));
		}
		
		if (isFileModified(artifact)) {
			File pluginsDir = getPluginsDir();
			File target = new File(pluginsDir, artifact.getName());
			try {
				Files.copy(artifact.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				throw new RuntimeException(String.format("Can't copy file '%s' to '%s'.",
						artifact.getPath(), pluginsDir.getPath()), e);
			}
		}
	}

	private boolean isFileModified(File artifact) {
		File pluginsDir = getPluginsDir();
		File existing = new File(pluginsDir, artifact.getName());
		if (!existing.exists())
			return true;
		
		return existing.lastModified() != artifact.lastModified();
	}

	private void updateSubsystem(String subsystem, boolean clean, List<String> updatedBundles) {
		String subsystemFullName = null;
		File subsystemProjectDir = null;
		
		if (subsystem.startsWith(GRANITE_SUBSYSTEM_PREFIX)) {
			subsystemFullName = GRANITE_PROJECT_PACKAGE_PREFIX + subsystem.substring(8);
			subsystemProjectDir = new File(options.getGraniteProjectDirPath(), subsystemFullName);
		} else if (subsystem.startsWith(SAND_SUBSYSTEM_PREFIX)) {
			subsystemFullName = options.getSandProjectName() + '.' + subsystem.substring(5);
			subsystemProjectDir = new File(options.getSandProjectDirPath(), subsystemFullName);
		} else {
			throw new IllegalArgumentException(String.format("Illegal subsystem name '%s'", subsystem));
		}
		if (!subsystemProjectDir.exists()) {
			throw new RuntimeException(String.format("Subsystem[%s] project directory[%s] doesn't exist.", subsystem, subsystemProjectDir.getPath()));
		}
		
		if (clean) {
			Main.runMvn(subsystemProjectDir, options.isOffline(), "clean", "package");
		} else {
			Main.runMvn(subsystemProjectDir, options.isOffline(), "package");
		}
		
		String[] bundles = subsystems.get(subsystem);
		if (bundles == null)
			return;
		
		for (String bundle : bundles) {
			BundleInfo bundleInfo = bundleInfos.get(bundle);
			
			if (bundleInfo == null) {
				throw new RuntimeException(String.format("Can't get bundle info for bundle %s.", bundle));
			}
			
			updateBundle(bundleInfo);
			updatedBundles.add(bundleInfo.fileName);
		}
	}

	private void loadCache() {
		if (!isCacheCreated()) {
			createCache();
		}
		
		loadCacheFromDisk();
	}

	private void loadCacheFromDisk() {
		loadBundleInfosFromDisk();
		loadSubsystemsFromDisk();
	}

	private void loadSubsystemsFromDisk() {
		File cacheDir = new File(options.getTargetDirPath(), DIRECTORY_NAME_CACHE);
		
		Properties pSubsystems = new Properties();
		
		Reader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File(cacheDir, FILE_NAME_SUBSYSTEMS)));
			pSubsystems.load(reader);
		} catch (IOException e) {
			throw new RuntimeException("Can't load cache from disk.", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
		}
		
		subsystems = new HashMap<>();
		for (Map.Entry<Object, Object> entry : pSubsystems.entrySet()) {
			String subsystemsName = (String)entry.getKey();
			String[] bundles = stringToArray((String)entry.getValue());
			
			subsystems.put(subsystemsName, bundles);
		}
		
	}

	private String[] stringToArray(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		int count = st.countTokens();
		String[] array = new String[count];
		
		for (int i = 0; i < count; i++) {
			array[i] = st.nextToken();
		}
		
		return array;
	}

	private void loadBundleInfosFromDisk() {
		File cacheDir = new File(options.getTargetDirPath(), DIRECTORY_NAME_CACHE);
		
		Properties pBundleInfos = new Properties();
		
		Reader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File(cacheDir, FILE_NAME_BUNDLEINFOS)));
			pBundleInfos.load(reader);
		} catch (IOException e) {
			throw new RuntimeException("Can't load cache from disk.", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
		}
		
		bundleInfos = new HashMap<>();
		for (Map.Entry<Object, Object> entry : pBundleInfos.entrySet()) {
			String bundleName = (String)entry.getKey();
			BundleInfo bundleInfo = stringToBundleInfo((String)entry.getValue());
			
			bundleInfos.put(bundleName, bundleInfo);
		}
	}

	private BundleInfo stringToBundleInfo(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		if (st.countTokens() != 2) {
			throw new RuntimeException("Bad cache format[Bundle Info].");
		}
		
		BundleInfo bundleInfo = new BundleInfo();
		bundleInfo.projectDirPath = st.nextToken();
		bundleInfo.fileName = st.nextToken();
		
		return bundleInfo;
	}

	private void createCache() {
		File cacheDir = new File(options.getTargetDirPath(), DIRECTORY_NAME_CACHE);
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			throw new RuntimeException("Can't create cache directory.");
		}
		
		File pluginsDir = getPluginsDir();
		
		for (File plugin : pluginsDir.listFiles()) {
			String pluginFileName = plugin.getName();
			if (!isGraniteArtifact(pluginFileName) && !isSandArtifact(pluginFileName)) {
				continue;
			}
			
			int dashIndex = pluginFileName.indexOf('-');
			String bundleName = pluginFileName.substring(0, dashIndex);
			
			BundleInfo bundleInfo = new BundleInfo();
			bundleInfo.fileName = pluginFileName;
			
			bundleInfos.put(bundleName, bundleInfo);
		}
		
		collectCacheData();
		
		syncCacheToDisk(cacheDir);
	}

	private File getPluginsDir() {
		File appDir = new File(options.getTargetDirPath(), options.getAppName());
		if (!appDir.exists()) {
			throw new RuntimeException("App directory doesn't exist. Please extract zip file first.");
		}
		
		File pluginsDir = new File(appDir, "plugins");
		if (!pluginsDir.exists()) {
			throw new RuntimeException("Plugins directory doesn't exist.");
		}
		return pluginsDir;
	}

	private void syncCacheToDisk(File cacheDir) {
		syncBundleInfosToDisk(cacheDir);
		syncSubsystemsToDisk(cacheDir);
	}

	private void syncBundleInfosToDisk(File cacheDir) {
		Properties pBundleInfos = new Properties();
		for (Map.Entry<String, BundleInfo> entry : bundleInfos.entrySet()) {
			pBundleInfos.put(entry.getKey(), convertBundleInfoToString(entry.getValue()));
		}
		
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(cacheDir, FILE_NAME_BUNDLEINFOS)));
			pBundleInfos.store(writer, null);
		} catch (IOException e) {
			throw new RuntimeException("Can't sync cache to disk.", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private Object convertBundleInfoToString(BundleInfo bundleInfo) {
		StringBuilder sb = new StringBuilder();
		sb.append(bundleInfo.projectDirPath).
			append(',').
			append(bundleInfo.fileName);
		
		return sb.toString();
	}

	private void syncSubsystemsToDisk(File cacheDir) {
		Properties pSubsystems = new Properties();
		for (Map.Entry<String, String[]> entry : subsystems.entrySet()) {
			pSubsystems.put(entry.getKey(), convertArrayToString(entry.getValue()));
		}
		
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(cacheDir, FILE_NAME_SUBSYSTEMS)));
			pSubsystems.store(writer, null);
		} catch (IOException e) {
			throw new RuntimeException("Can't sync cache to disk.", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private String convertArrayToString(String[] array) {
		StringBuilder sb = new StringBuilder();
		
		for (String string : array) {
			sb.append(string).append(',');
		}
		
		sb.deleteCharAt(sb.length() - 1); // delete last comma
		
		return sb.toString();	
	}

	private void collectCacheData() {
		File graniteProjectDir = new File(options.getGraniteProjectDirPath());
		collectCacheData(graniteProjectDir, null);
		
		if (options.getSandProjectDirPath() == null)
			return;
		
		File sandProjectDir = new File(options.getSandProjectDirPath());
		collectCacheData(sandProjectDir, null);
	}

	private void collectCacheData(File currentDir, String subsystem) {
		if (DIRECTORY_NAME_DOT_GIT.equals(currentDir.getName()))
			return;
		
		if (isArtifactProject(currentDir)) {
			BundleInfo bundleInfo = bundleInfos.get(currentDir.getName());
			bundleInfo.projectDirPath = currentDir.getPath();
			
			if (subsystem != null) {
				String[] bundles = subsystems.get(subsystem);
				if (bundles == null) {
					bundles = new String[] {currentDir.getName()};
				} else {
					String[] newBundles = Arrays.copyOf(bundles, bundles.length + 1);
					newBundles[newBundles.length - 1] = currentDir.getName();
					bundles = newBundles;
				}
				
				subsystems.put(subsystem, bundles);
			}
			
			return;
		}
		
		for (File file : currentDir.listFiles()) {
			if (file.isDirectory() && !DIRECTORY_NAME_DOT_GIT.equals(file.getName())) {
				collectCacheData(file, subsystem == null ? getSubsystem(file) : subsystem);
			}
		}
	}

	private String getSubsystem(File file) {
		String fileName = file.getName();
		for (String subsystemName : getSubsystems()) {
			if (fileName.endsWith("." + subsystemName)) {
				return subsystemName;
			}
		}
		
		return null;
	}

	private boolean isArtifactProject(File dir) {
		boolean pomFound = false;
		boolean srcFound = false;
		
		for (File file : dir.listFiles()) {
			if (file.getName().equals("src") && file.isDirectory()) {
				srcFound = true;
			} else if (file.getName().equals("pom.xml")) {
				pomFound = true;
			} else {
				continue;
			}
			
			if (srcFound && pomFound) {
				break;
			}
		}
		
		return srcFound && pomFound && bundleInfos.containsKey(dir.getName());
	}

	private class BundleInfo {
		public String fileName;
		public String projectDirPath;
	}

	private boolean isGraniteArtifact(String pluginFileName) {
		return pluginFileName.startsWith(GRANITE_PROJECT_PACKAGE_PREFIX);
	}
	
	private boolean isSandArtifact(String pluginFileName) {
		if (options.getSandProjectName() == null)
			return false;
		
		return pluginFileName.startsWith(options.getSandProjectName() + '.');
	}

	private boolean isCacheCreated() {
		File cacheDir = new File(options.getTargetDirPath(), DIRECTORY_NAME_CACHE);
		if (!cacheDir.exists())
			return false;
		
		return new File(cacheDir, FILE_NAME_SUBSYSTEMS).exists() && new File(cacheDir, FILE_NAME_BUNDLEINFOS).exists();
	}
	
	private boolean isSubsystem(String module) {
		for (String subsystemName : getSubsystems()) {
			if (subsystemName.equals(module))
				return true;
		}
		
		return false;
	}
}
