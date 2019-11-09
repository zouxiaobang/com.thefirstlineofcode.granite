package com.firstlinecode.granite.framework.supports.spring;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.BeanFactory;

import com.firstlinecode.granite.framework.core.supports.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.supports.IApplicationComponentServiceAware;
import com.firstlinecode.granite.framework.core.repository.CreationException;
import com.firstlinecode.granite.framework.core.repository.IComponentInfo;
import com.firstlinecode.granite.framework.core.repository.IDependencyInfo;

public class SpringComponentInfo implements IComponentInfo, IApplicationComponentServiceAware {
	
	private String id;
	private String targetBeanName;
	private BeanFactory beanFactory;
	private BundleContext bundleContext;
	
	public SpringComponentInfo(String id, String targetBeanName, BeanFactory beanFactory, BundleContext bundleContext) {
		this.id = id;
		this.targetBeanName = targetBeanName;
		this.beanFactory = beanFactory;
		this.bundleContext = bundleContext;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void addDependency(IDependencyInfo dependency) {
		throw new UnsupportedOperationException("Spring component doesn't support addDependency method.");
	}

	@Override
	public void removeDependency(IDependencyInfo dependency) {
		throw new UnsupportedOperationException("Spring component doesn't support removeDependency method.");
	}

	@Override
	public IDependencyInfo[] getDependencies() {
		return new IDependencyInfo[0];
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public boolean isService() {
		return false;
	}

	@Override
	public Object create() throws CreationException {
		return beanFactory.getBean(targetBeanName);
	}

	@Override
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	@Override
	public IComponentInfo getAliasComponent(String alias) {
		throw new UnsupportedOperationException("alias not allowed");
	}
	
	@Override
	public String toString() {
		return String.format("Spring Component[id: %s, target bean name: %s]", id, targetBeanName) ;
	}

	@Override
	public boolean isSingleton() {
		return beanFactory.isSingleton(targetBeanName);
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		if (isSingleton()) {
			appComponentService.inject(beanFactory.getBean(targetBeanName), bundleContext);
		}		
	}

}
