package com.thefirstlineofcode.granite.pack.cluster.mgtnode;

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
	private static final String GRANITE_PROJECT_PACKAGE_PREFIX = "com.firstlinecode.granite.";

	private static final String DIRECTORY_NAME_CACHE = ".cache";

	private static final String FILE_NAME_SUBSYSTEMS = "subsystems.ini";

	private static final String FILE_NAME_BUNDLEINFOS = "bundleinfos.ini";

	private static final String[] SUBSYSTEM_NAMES = new String[] {
			"framework",
			"im",
			"stream",
			"xeps",
			"leps",
			"deploy.cluster"
	};
	
	private Map<String, String[]> subsystems;
	private Map<String, BundleInfo> bundleInfos;
	
	private Options options;
	
	private File repositoryDir;
	
	public Updater(Options options) {
		this.options = options;
		subsystems = new HashMap<>(6);
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
			modules = SUBSYSTEM_NAMES;
		
		List<String> updatedBundles = new ArrayList<>();
		for (String module : modules) {
			if (isSubsystem(module)) {
				updateSubsystem(module, clean, updatedBundles);
			} else {
				if (!module.startsWith(GRANITE_PROJECT_PACKAGE_PREFIX)) {
					module = GRANITE_PROJECT_PACKAGE_PREFIX + module;
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
	
	private void updateBundle(String bundle, boolean clean, List<String> updatedBundles) {
		BundleInfo bundleInfo = bundleInfos.get(bundle);
		if (clean) {
			Main.runMvn(new File(bundleInfo.projectDirPath), "clean", "package");
		} else {
			Main.runMvn(new File(bundleInfo.projectDirPath), "package");
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
			File repositoryDir = getRepositoryDir();
			File target = new File(repositoryDir, artifact.getName());
			try {
				Files.copy(artifact.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				throw new RuntimeException(String.format("Can't copy file '%s' to '%s'.",
						artifact.getPath(), repositoryDir.getPath()), e);
			}
		}
	}

	private boolean isFileModified(File artifact) {
		File repositoryDir = getRepositoryDir();
		File existing = new File(repositoryDir, artifact.getName());
		if (!existing.exists())
			return true;
		
		return existing.lastModified() != artifact.lastModified();
	}

	private void updateSubsystem(String subsystem, boolean clean, List<String> updatedBundles) {
		String subsystemFullName = GRANITE_PROJECT_PACKAGE_PREFIX + subsystem;
		File subsystemProjectDir = new File(options.getGraniteProjectDirPath(), subsystemFullName);
		if (!subsystemProjectDir.exists()) {
			throw new RuntimeException(String.format("Subsystem[%s] project directory doesn't exist.", subsystem));
		}
		
		if (clean) {
			Main.runMvn(subsystemProjectDir, "clean", "package");
		} else {
			Main.runMvn(subsystemProjectDir, "package");
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
		
		File repositoryDir = getRepositoryDir();
		
		for (File bundle : repositoryDir.listFiles()) {
			String bundleFileName = bundle.getName();
			if (!isGraniteArtifact(bundleFileName)) {
				continue;
			}
			
			int dashIndex = bundleFileName.indexOf('-');
			String bundleName = bundleFileName.substring(0, dashIndex);
			
			BundleInfo bundleInfo = new BundleInfo();
			bundleInfo.fileName = bundleFileName;
			
			bundleInfos.put(bundleName, bundleInfo);
		}
		
		collectCacheData();
		
		syncCacheToDisk(cacheDir);
	}

	private File getRepositoryDir() {
		if (repositoryDir != null)
			return repositoryDir;
		
		if (options.getRepositoryDirPath() != null) {
			repositoryDir = new File(options.getRepositoryDirPath());
		}
		
		if (repositoryDir == null) {
			File appDir = new File(options.getTargetDirPath(), options.getAppName());
			if (!appDir.exists()) {
				throw new RuntimeException("App directory doesn't exist. Please extract zip file first.");
			}
			
			repositoryDir = new File(appDir, "repository");
		}
		
		if (!repositoryDir.exists()) {
			throw new RuntimeException(String.format("Repository directory %s doesn't exist.", repositoryDir.getPath()));
		}
		
		return repositoryDir;
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
	}

	private void collectCacheData(File currentDir, String subsystem) {
		if (".git".equals(currentDir.getName()))
			return;
		
		if (isGraniteArtifactProject(currentDir)) {
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
			if (file.isDirectory()) {
				collectCacheData(file, subsystem == null ? getSubsystem(file) : subsystem);
			}
		}
	}

	private String getSubsystem(File file) {
		String fileName = file.getName();
		for (String subsystemName : SUBSYSTEM_NAMES) {
			if (fileName.endsWith("." + subsystemName)) {
				return subsystemName;
			}
		}
		
		return null;
	}

	private boolean isGraniteArtifactProject(File dir) {
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

	private boolean isGraniteArtifact(String bundleFileName) {
		return bundleFileName.startsWith(GRANITE_PROJECT_PACKAGE_PREFIX);
	}

	private boolean isCacheCreated() {
		File cacheDir = new File(options.getTargetDirPath(), DIRECTORY_NAME_CACHE);
		if (!cacheDir.exists())
			return false;
		
		return new File(cacheDir, FILE_NAME_SUBSYSTEMS).exists() && new File(cacheDir, FILE_NAME_BUNDLEINFOS).exists();
	}
	
	private boolean isSubsystem(String module) {
		for (String subsystemName : SUBSYSTEM_NAMES) {
			if (subsystemName.equals(module))
				return true;
		}
		
		return false;
	}
}
