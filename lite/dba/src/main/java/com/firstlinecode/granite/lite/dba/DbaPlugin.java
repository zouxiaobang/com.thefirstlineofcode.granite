package com.firstlinecode.granite.lite.dba;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.adf.data.IDataObjectFactory;
import com.firstlinecode.granite.framework.core.repository.IRepository;
import com.firstlinecode.granite.framework.core.repository.IRepositoryAware;

public class DbaPlugin extends Plugin implements IRepositoryAware, IApplicationComponentServiceAware {
	private IRepository repository;
	private IApplicationComponentService appComponentService;

	public DbaPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}
	
	@Override
	public void start() {
		IDataObjectFactory dataObjectFactory = new DataObjectFactory();
		// TODO Auto-generated method stub
		super.start();
	}
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRepository(IRepository repository) {
		this.repository = repository;
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		// TODO Auto-generated method stub
		
	}
}
