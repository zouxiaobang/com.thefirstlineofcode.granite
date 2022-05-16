package com.thefirstlineofcode.granite.pack.lite;

import java.util.Arrays;

public class Main {
	private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";
	private static final String NAME_PREFIX_APP = "granite-lite";
	private static final String DEFAULT_SAND_PROJECT_NAME = "com.thefirstlineofcode.sand";
	
	private Options options;
	
	public static void main(String[] args) {
		Main main = new Main();
		main.run(args);
	}
	
	public void run(String[] args) {
		try {
			options = parseOptions(args);
		} catch (IllegalArgumentException e) {
			if (e.getMessage() != null)
				System.out.println("Error: " + e.getMessage());
			printUsage();
			return;
		}
		
		if (options.isHelp()) {
			printUsage();
			return;
		}
		
		if (options.isPack()) {
			new Packer(options).pack();
		} else {
			Updater updater = new Updater(options);
			
			if (options.isCleanCache()) {
				updater.cleanCache();
			}
			
			if (options.isCleanUpdate()) {
				updater.update(true);
			} else {
				updater.update(false);
			}
		}
	}
	
	private Options parseOptions(String[] args) {
		Options options = new Options();
		
		if (args.length == 1 && args[0].equals("-help")) {
			options.setHelp(true);
			
			return options;
		}
		
		int i = 0;
		while (i < args.length) {
			if ("-update".equals(args[i])) {
				options.setUpdate(true);
				i++;
			} else if ("-cleanUpdate".equals(args[i])) {
				options.setCleanUpdate(true);
				i++;
			} else if ("-cleanCache".equals(args[i])) {
				options.setCleanCache(true);
				i++;
			} else if ("-version".equals(args[i])) {
				if (i == (args.length - 1)) {
					throw new IllegalArgumentException("-version should follow a <VERSION> option value.");
				}
				i++;
				
				if (args[i].startsWith("-")) {
					throw new IllegalArgumentException("-version should follow a <VERSION> option value.");
				}
				
				options.setVersion(args[i]);
				i++;
			} else if("-protocol".equals(args[i])) {
				if (i == (args.length - 1)) {
					throw new IllegalArgumentException("-protocol should follow a <PROTOCOL> option value.");
				}
				i++;
				
				if (args[i].startsWith("-")) {
					throw new IllegalArgumentException("-protocol should follow a <PROTOCOL> option value.");
				}
				
				if ("standard".equals(args[i])) {
					options.setProtocol(Options.Protocol.STANDARD);
				} else if ("sand".equals(args[i])) {
					options.setProtocol(Options.Protocol.SAND);
				} else {
					throw new IllegalArgumentException(String.format("Illegal protocol: %s. Only 'standard' and 'sand' supported.", args[i]));
				}
				i++;
			} else if ("sandProjectName".equals(args[i])) {
				if (i == (args.length - 1)) {
					throw new IllegalArgumentException("-sandProjectName should follow a <SAND-PROJECT-NAME> option value.");
				}
				i++;
				
				if (args[i].startsWith("-")) {
					throw new IllegalArgumentException("-sandProjectName should follow a <SAND-PROJECT-NAME> option value.");
				}
				
				options.setSandProjectName(args[i]);
			} else if ("-commerical".equals(args[i])) {
				options.setCommerical(true);
				i++;
			} else if ("-offline".equals(args[i])) {
				options.setOffline(true);
				i++;
			} else {
				options.setModules(Arrays.copyOfRange(args, i, args.length));
				break;
			}
		}
		
		if (options.isUpdate() && options.isCleanUpdate()) {
			throw new IllegalArgumentException("You can specify option -update or -cleanUpdate. But not both.");
		}
		
		if (!options.isUpdate() && !options.isCleanUpdate() && options.getModules() != null) {
			throw new IllegalArgumentException("[BUNDLE_SYMBOLIC_NAME]... is only used in update mode. Maybe you should add -update or -clean-update to options.");
		}
		
		if (options.getVersion() == null) {
			options.setVersion(DEFAULT_VERSION);
		}
		
		options.setAppName(String.format("%s-%s-%s", NAME_PREFIX_APP, options.getProtocol().toString().toLowerCase(), options.getVersion()));
		
		if (!options.isUpdate() && !options.isCleanUpdate() && options.getProtocol() == null) {
			options.setProtocol(Options.Protocol.STANDARD);
		}
		
		options.setTargetDirPath(PackUtils.getTargetDirPath(this));
		options.setProjectDirPath(PackUtils.getProjectDirPath(options.getTargetDirPath()));
		options.setGraniteProjectDirPath(PackUtils.getGraniteProjectDirPath(options.getProjectDirPath()));
		if (options.getSandProjectName() == null) {
			options.setSandProjectName(DEFAULT_SAND_PROJECT_NAME);
		}
		options.setSandProjectDirPath(PackUtils.getSandProjectDirPath(options.getProjectDirPath(), options.getSandProjectName()));
		
		return options;
	}
	
	private void printUsage() {
		System.out.println("Usage:");
		System.out.println("java -jar granite-pack-lite-${VERSION}.jar [OPTIONS] [Bundle-SymbolicNames or SubSystems]");
		System.out.println("OPTIONS:");
		System.out.println("-help                                  Display help information.");
		System.out.println("-update                                Update specified modules.");
		System.out.println("-cleanUpdate                           Clean and update specified modules.");
		System.out.println("-cleanCache                            Clean the packing cache.");
		System.out.println("-offline                               Run in offline mode.");
		System.out.println("-version <VERSION>                     Specify the version. Default is 0.2.1-RELEASE.");
		System.out.println("-protocol <PROTOCOL>                   Specify the protocol. Optional protocols are 'standard' or 'sand'. Default is 'standard').");
		System.out.println("-sandProjectName <SAND-PROJECT-NAME>   Specify the sand project name. Default is 'com.thefirstlineofcode.sand'.");
	}
}