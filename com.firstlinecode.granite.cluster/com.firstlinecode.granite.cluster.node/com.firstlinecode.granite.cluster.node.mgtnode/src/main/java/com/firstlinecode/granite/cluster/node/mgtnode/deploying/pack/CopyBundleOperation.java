package com.firstlinecode.granite.cluster.node.mgtnode.deploying.pack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyBundleOperation {
	private static final Logger logger = LoggerFactory.getLogger(CopyBundleOperation.class);
	
	private String bundleName;
	
	public CopyBundleOperation(String bundleName) {
		this.bundleName = bundleName;
	}

	public void copy(IPackContext context) {
		File repositoryDir = context.getRepositoryDir().toFile();
		File bundle = null;
		for (File aBundle : repositoryDir.listFiles()) {
			if (aBundle.getName().startsWith(bundleName)) {
				bundle = aBundle;
				break;
			}
		}
		
		if (bundle == null) {
			throw new RuntimeException(String.format("Bundle not found. Bundle name %s.", bundleName));
		}
		
		Path target = new File(context.getRuntimePluginsDir().toFile(), bundle.getName()).toPath();
		try {
			if (target.toFile().exists()) {
				logger.warn("Bundle {} has existed. Ignore the copy bundle operation.", target.getFileName());
				return;
			}
			
			Files.copy(bundle.toPath(), target, StandardCopyOption.COPY_ATTRIBUTES,
					StandardCopyOption.REPLACE_EXISTING);
			logger.debug("Copy bundle {} from repository position[{}] to runtime plugins directory[{}].",
					new Object[] {bundleName, bundle.getPath(), target.toFile().getPath()});
		} catch (IOException e) {
			throw new RuntimeException(String.format("Can't copy bundle %s from repository to runtime plugins directory %s.",
					bundle.getPath(), context.getRuntimePluginsDir().toFile().getPath()), e);
		}
	}

}
