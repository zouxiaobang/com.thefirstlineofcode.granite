package com.thefirstlineofcode.granite.cluster.pipeline;

import java.io.File;

import org.apache.ignite.Ignite;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlan;
import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlanException;
import com.thefirstlineofcode.granite.cluster.node.commons.deploying.DeployPlanReader;
import com.thefirstlineofcode.granite.framework.core.repository.IInitializable;
import com.thefirstlineofcode.granite.framework.core.repository.IRepository;
import com.thefirstlineofcode.granite.framework.core.repository.IRepositoryAware;

@Component
public class DeployClusterComponentsRegistrar implements IRepositoryAware, ApplicationContextAware, IInitializable {
	private static final String PROPERTY_KEY_NODE_TYPE = "granite.node.type";
	private static final String PROPERTY_KEY_GRANITE_DEPLOY_PLAN_FILE = "granite.deploy.plan.file";
	
	private IRepository repository;
	private ApplicationContext applicationContext;
	
	@Override
	public void init() {
		Ignite ignite = applicationContext.getBean(Ignite.class);
		if (ignite == null)
			throw new RuntimeException("Null ignite instance.");
		
		repository.registerSingleton(Constants.COMPONENT_ID_IGNITE, ignite);
		
		DeployPlan deployPlan = readDeployPlan();
		repository.registerSingleton(Constants.COMPONENT_ID_CLUSTER_NODE_RUNTIME_CONFIGURATION,
				new NodeRuntimeConfiguration(System.getProperty(PROPERTY_KEY_NODE_TYPE), deployPlan));
	}
	
	private DeployPlan readDeployPlan() {
		String deployFilePath = System.getProperty(PROPERTY_KEY_GRANITE_DEPLOY_PLAN_FILE);
		
		try {
			return new DeployPlanReader().read(new File(deployFilePath).toPath());
		} catch (DeployPlanException e) {
			throw new RuntimeException("Can't read deploy configuration file.", e);
		}
	}
	
	@Override
	public void setRepository(IRepository repository) {
		this.repository = repository;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
