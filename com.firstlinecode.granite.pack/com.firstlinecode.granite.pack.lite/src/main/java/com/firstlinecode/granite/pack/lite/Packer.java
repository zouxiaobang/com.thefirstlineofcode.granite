package com.firstlinecode.granite.pack.lite;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Packer {
	private static final String CONFIGURATION_DIR = "/configuration/com.firstlinecode.granite/";
	public static final String COMPONENT_BINDING_LITE_CONFIG_FILE = "component-binding-lite.ini";
	private static final String SAND_COMPONENT_BINDING_LITE_CONFIG_FILE = "sand-component-binding-lite.ini";
	private static final String APPLICATION_CONFIG_FILE = "application.ini";
	private static final String SAND_APPLICATION_CONFIG_FILE = "sand-application.ini";
	private static final String NAME_PREFIX_OSGI_BUNDLE = "org.eclipse.osgi-";
	private static final String NAME_PREFIX_ECLIPSE_COMMON_BUNDLE = "org.eclipse.equinox.common-";
	private static final String NAME_PREFIX_ECLIPSE_UPDATE_BUNDLE = "org.eclipse.update.configurator-";
	
	private Options options;
	
	public Packer(Options options) {
		this.options = options;
	}
	
	public void pack() {
		File dependencyDir = new File(options.getTargetDirPath(), "dependency");
		deleteDependencies(dependencyDir);
		
		copyDependencies();
		
		File zip = new File(options.getTargetDirPath(), options.getAppName() + ".zip");
		if (zip.exists()) {
			System.out.println(String.format("Zip file %s has existed. delete it...", zip.getPath()));
			if (!zip.delete())
				throw new RuntimeException(String.format("Can't delete file %s.", zip.getPath()));
		}
		
		System.out.println(String.format("Create zip file %s...", zip.getName()));
		File[] bundles = dependencyDir.listFiles();
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)));
			
			for (File bundle : bundles) {
				writeBundleToZip(zos, bundle);
			}
			
			writeConfigIniFileToZip(zos, bundles);
			
			writeGraniteConfigFilesToZip(options.getTargetDirPath(), zos);
			
			System.out.println(String.format("Zip file %s has created.", zip.getName()));
		} catch (Exception e) {
			throw new RuntimeException("Can't create granite lite package.", e);
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void deleteDependencies(File dependencyDir) {
		if (dependencyDir.exists()) {
			System.out.println(String.format("Dependency directory %s has existed. Delete it...", dependencyDir.getPath()));
			
			File[] files = dependencyDir.listFiles();
			
			for (File file : files) {
				try {
					Files.delete(file.toPath());
				} catch (IOException e) {
					throw new RuntimeException(String.format("Can't delete file %s.", file.getPath()), e);
				}
			}
			
			try {
				Files.delete(dependencyDir.toPath());
			} catch (IOException e) {
				throw new RuntimeException(String.format("Can't delete dependency directory %s. Maybe you should delete it manually.",
						dependencyDir.getPath()), e);
			}
		}
	}
	
	private void copyDependencies() {
		if (options.getProtocol() == Options.Protocol.STANDARD) {
			Main.runMvn(new File(options.getProjectDirPath()), options.isOffline(), "-fstandard-pom.xml", "dependency:copy-dependencies");
		} else if (options.getProtocol() == Options.Protocol.LEP) {
			Main.runMvn(new File(options.getProjectDirPath()), options.isOffline(), "-fleps-pom.xml", "dependency:copy-dependencies");
		} else {
			Main.runMvn(new File(options.getProjectDirPath()), options.isOffline(), "-fsand-pom.xml", "dependency:copy-dependencies");			
		}
		
		if (options.isCommerical()) {
			Main.runMvn(new File(options.getProjectDirPath()), options.isOffline(), "-fgem-pom.xml", "dependency:copy-dependencies");
		}
	}

	private void writeGraniteConfigFilesToZip(String targetDirPath, ZipOutputStream zos) throws IOException, URISyntaxException {
		File targetDir = new File(targetDirPath);
		File configFilesDir = new File(targetDir.getParent(), "config");
		
		for (File configFile : configFilesDir.listFiles()) {
			if (options.getProtocol() != Options.Protocol.SAND) {
				if (SAND_APPLICATION_CONFIG_FILE.equals(configFile.getName()) ||
						SAND_COMPONENT_BINDING_LITE_CONFIG_FILE.equals(configFile.getName()))
					continue;
				
				writeFileToZip(zos, options.getAppName() + CONFIGURATION_DIR + configFile.getName(), configFile);
			} else {
				if (APPLICATION_CONFIG_FILE.equals(configFile.getName()) ||
						COMPONENT_BINDING_LITE_CONFIG_FILE.equals(configFile.getName())) {
					continue;
				} else if (configFile.getName().equals(SAND_APPLICATION_CONFIG_FILE)) {					
					writeFileToZip(zos, options.getAppName() + CONFIGURATION_DIR + APPLICATION_CONFIG_FILE, configFile);					
				} else {
					writeFileToZip(zos, options.getAppName() + CONFIGURATION_DIR + configFile.getName(), configFile);					
				}
			}

		}
	}

	private void writeConfigIniFileToZip(ZipOutputStream zos, File[] bundles) throws IOException {
		try {
			zos.putNextEntry(new ZipEntry(options.getAppName() + "/configuration/config.ini"));
			zos.write(generateConfigIniFileContent(bundles).getBytes());
		} finally {
			zos.closeEntry();
		}
	}

	private String generateConfigIniFileContent(File[] bundles) {
		String eclipseCommonBundleName = null;
		String eclipseUpdateBundleName = null;
		StringBuilder bundlesReference = new StringBuilder();
		for (File bundle : bundles) {
			if (eclipseCommonBundleName == null && bundle.getName().startsWith(NAME_PREFIX_ECLIPSE_COMMON_BUNDLE)) {
				eclipseCommonBundleName = bundle.getName();
			} else if (eclipseUpdateBundleName == null && bundle.getName().startsWith(NAME_PREFIX_ECLIPSE_UPDATE_BUNDLE)) {
				eclipseUpdateBundleName = bundle.getName();
			} else if (bundle.getName().startsWith(NAME_PREFIX_OSGI_BUNDLE)) {
				continue;
			} else {
				bundlesReference.
					append(",\\").
					append("\r\n").
					append("reference:file:plugins/").
					append(bundle.getName()).
					append("@start");
			}
		}
		
		if (eclipseCommonBundleName == null || eclipseUpdateBundleName == null) {
			throw new RuntimeException("Eclipse common bundle or eclipse update bundle could not be found.");
		}
		
		StringBuilder content = new StringBuilder();
		content.
			append("osgi.bundles=\\").
			append("\r\n").
			append("reference:file:plugins/").
			append(eclipseCommonBundleName).append("@2:start").
			append(",\\").
			append("\r\n").
			append("reference:file:plugins/").
			append(eclipseUpdateBundleName).append("@3:start").
			append(bundlesReference.toString());
		
		content.
			append("\r\n").
			append("eclipse.ignoreApp=true");
		
		content.
			append("\r\n").
			append("osgi.bundles.defaultStartLevel=4");
		
		// XPathFactory uses com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl(in JDK) as default XPathFactory implementation
		content.
			append("\r\n").
			append("org.osgi.framework.bootdelegation=com.sun.org.apache.xpath.internal.jaxp");
		
		content.append("\r\n");
		
		return content.toString();
	}

	private void writeBundleToZip(ZipOutputStream zos, File bundle) throws IOException {
		if (bundle.getName().startsWith(NAME_PREFIX_OSGI_BUNDLE)) {
			writeFileToZip(zos, options.getAppName() + "/" + bundle.getName(), bundle);
		} else {
			writeFileToZip(zos, options.getAppName() + "/plugins/" + bundle.getName(), bundle);
		}
		
	}
	
	private void writeFileToZip(ZipOutputStream zos, String entryPath, File file) throws IOException {
		BufferedInputStream bis = null;
		try {
			zos.putNextEntry(new ZipEntry(entryPath));
			bis = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[2048];
			
			int size = -1;
			while ((size = bis.read(buf)) != -1) {
				zos.write(buf, 0, size);
			}
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (Exception e) {
					// ignore
				}
			}
			zos.closeEntry();
		}
	}
}
