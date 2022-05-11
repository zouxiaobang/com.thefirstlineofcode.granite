package com.thefirstlineofcode.granite.pack.lite;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Packer {
	public static final String CONFIGURATION_DIR = "configuration";
	
	private Options options;
	private List<String> systemLibraries;
	
	public Packer(Options options) {
		this.options = options;
		systemLibraries = new ArrayList<>();
	}
	
	public void pack() {
		File dependencyDir = new File(options.getTargetDirPath(), "dependency");
		deleteDependenciesDirectory(dependencyDir);
		
		copyDependencies();
		
		String zipName = options.getAppName() + ".zip";
		File zip = new File(options.getTargetDirPath(), zipName);
		if (zip.exists()) {
			System.out.println(String.format("Zip file %s has existed. delete it...", zipName));
			if (!zip.delete())
				throw new RuntimeException(String.format("Can't delete file %s.", zipName));
		}
		
		System.out.println(String.format("Create zip file %s...", zip.getName()));
		
		ZipOutputStream zos = null;
		try {
			File basicServerZip = getBasicServerZip();
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)));
			
			copyBasicServerToZip(basicServerZip, zos);
			
			File[] dependencies = dependencyDir.listFiles();
			if (dependencies != null && dependencies.length > 0) {
				for (File dependency : dependencies) {
					if (isPlugin(dependency.getName())) {
						System.out.println(dependency);
						writeToPlugins(zos, dependency);
					}
				}
			}
			
			writeGraniteConfigFilesToZip(options.getTargetDirPath(), zos);
			
			System.out.println(String.format("Zip file %s has created.", zipName));
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

	private boolean isPlugin(String dependency) {
		return !systemLibraries.contains(dependency);
	}

	private void copyBasicServerToZip(File basicServerZip, ZipOutputStream zos) {		
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(basicServerZip)));
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				String entryName = entry.getName();
				if (isSystemLibrary(entryName)) {
					int lastSlash = entryName.lastIndexOf('/');
					String libraryName = entryName.substring(lastSlash + 1);
					systemLibraries.add(libraryName);
				}
				
				writeEntryToZip(zis, zos, entry);
			}
		} catch (IOException e) {
			throw new RuntimeException("Can't copy basic server to zip file.", e);
		} finally {
			if (zis != null)
				try {
					zis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private boolean isSystemLibrary(String entryName) {
		return entryName.endsWith(".jar") && entryName.indexOf("/libs/") != -1;
	}

	private void writeEntryToZip(ZipInputStream zis, ZipOutputStream zos, ZipEntry inEntry) throws IOException {
		ZipEntry outEntry = getOutEntry(inEntry);
		zos.putNextEntry(outEntry);
		
		int buffSize = 2048;
		byte[] buff = new byte[buffSize];
		int length = 0;
		while ((length = zis.read(buff, 0, buffSize)) != -1) {
			zos.write(buff, 0, length);			
		}
	}

	private ZipEntry getOutEntry(ZipEntry inEntry) {
		ZipEntry outEntry = new ZipEntry(inEntry);
		
		Field nameField = null;
		Boolean oldAccessiable = null;
		try {			
			nameField = ZipEntry.class.getDeclaredField("name");
			oldAccessiable = nameField.isAccessible();
			nameField.setAccessible(true);
			nameField.set(outEntry, getOutEntryName(inEntry.getName()));			
		} catch (Exception e) {
			throw new RuntimeException("Can't change zip entry's name.", e);
		} finally {
			if (oldAccessiable != null) {
				nameField.setAccessible(oldAccessiable);
			}
		}
		
		return outEntry;
	}

	private String getOutEntryName(String name) {
		int slashIndex = name.indexOf('/', 1);
		if (slashIndex == -1)
			throw new IllegalArgumentException("Can't find two slash");
		
		return String.format("%s/%s", options.getAppName(), name.substring(slashIndex + 1));
	}

	private File getBasicServerZip() {
		File serverDir = new File(options.getGraniteProjectDirPath(), "server");
		if (!serverDir.exists() || !serverDir.isDirectory())
			throw new RuntimeException("Can't determine granite server directory.");
		
		File serverTargetDir = new File(serverDir, "target");
		Main.runMvn(serverDir, options.isOffline(), "-fpack-lite-pom.xml", "clean", "package");
		
		if (!serverTargetDir.exists() || !serverTargetDir.isDirectory()) {
			throw new RuntimeException("Can't determine target directory of Granite Server project.");
		}
		
		for (File file : serverTargetDir.listFiles()) {
			if (file.getName().endsWith(".zip"))
				return file;
		}
		
		throw new IllegalStateException("Server zip can't be found. Maybe you should build the server project first.");
	}

	private void deleteDependenciesDirectory(File dependencyDir) {
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
		} else {
			Main.runMvn(new File(options.getProjectDirPath()), options.isOffline(), "-fsand-pom.xml", "dependency:copy-dependencies");			
		}
		
		if (options.isCommerical()) {
			Main.runMvn(new File(options.getProjectDirPath()), options.isOffline(), "-fgem-pom.xml", "dependency:copy-dependencies");
		}
	}

	private void writeGraniteConfigFilesToZip(String targetDirPath, ZipOutputStream zos) throws IOException, URISyntaxException {
		File targetDir = new File(targetDirPath);
		File configurationDir = new File(targetDir.getParent(), CONFIGURATION_DIR);
		
		File configurationFilesDir;
		if (options.getProtocol() == Options.Protocol.STANDARD) {
			configurationFilesDir = new File(configurationDir, "standard");			
		} else if (options.getProtocol() == Options.Protocol.SAND) {
			configurationFilesDir = new File(configurationDir, "sand");			
		} else {
			throw new IllegalArgumentException("Only support 'iot', 'standard' and 'csand' protocols now.");
		}
		
		for (File configurationFile : configurationFilesDir.listFiles()) {
			writeFileToZip(zos, String.format("/%s/%s/%s", options.getAppName(), CONFIGURATION_DIR, configurationFile.getName()), configurationFile);
		}
	}
	
	private void writeToPlugins(ZipOutputStream zos, File plugin) throws IOException {
		File systemLibrary = new File(options.getAppName() + "/libs/" + plugin.getName());
		// This is a system library. Don't write it as a plugin.
		if (systemLibrary.exists())
			return;
		
		writeFileToZip(zos, options.getAppName() + "/plugins/" + plugin.getName(), plugin);
		
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
