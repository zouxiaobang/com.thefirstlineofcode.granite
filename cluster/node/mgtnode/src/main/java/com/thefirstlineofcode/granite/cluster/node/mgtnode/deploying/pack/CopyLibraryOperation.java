package com.thefirstlineofcode.granite.cluster.node.mgtnode.deploying.pack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyLibraryOperation {
	private static final Logger logger = LoggerFactory.getLogger(CopyLibraryOperation.class);
	
	private String libraryName;
	private boolean optional;
	
	public CopyLibraryOperation(String libraryName) {
		this(libraryName, false);
	}
	
	public CopyLibraryOperation(String libraryName, boolean optional) {
		this.libraryName = libraryName;
		this.optional = optional;
	}

	public void copy(IPackContext context) {
		File repositoryDir = context.getRepositoryDir().toFile();
		File library = null;
		for (File aLibrary : repositoryDir.listFiles()) {
			if (aLibrary.getName().startsWith(libraryName)) {
				library = aLibrary;
				break;
			}
		}
		
		if (library == null && optional) {
			logger.info("Optinal library {} not found. Ignore to copy it.", libraryName);
			return;
		}
		
		if (library == null) {
			throw new RuntimeException(String.format("Library not found. Library name %s.", libraryName));
		}
		
		Path target = new File(context.getRuntimePluginsDir().toFile(), library.getName()).toPath();
		try {
			if (target.toFile().exists()) {
				logger.warn("Library {} has existed. Ignore to copy library.", target.getFileName());
				return;
			}
			
			Files.copy(library.toPath(), target, StandardCopyOption.COPY_ATTRIBUTES,
					StandardCopyOption.REPLACE_EXISTING);
			logger.debug("Copy library {} from repository position[{}] to runtime plugins directory[{}].",
					new Object[] {libraryName, library.getPath(), target.toFile().getPath()});
		} catch (IOException e) {
			throw new RuntimeException(String.format("Can't copy library %s from repository to runtime plugins directory %s.",
					library.getPath(), context.getRuntimePluginsDir().toFile().getPath()), e);
		}
	}

}
