package com.firstlinecode.granite.framework.supports.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.firstlinecode.granite.framework.core.repository.IComponentCollector;
import com.firstlinecode.granite.framework.core.repository.IComponentInfo;

public class SpringComponentFactoryBean implements FactoryBean<IComponentInfo>, BeanFactoryAware,
			InitializingBean, DisposableBean, BundleContextAware, ServiceListener {
	private String componentId;
	private String targetBeanName;
	private BeanFactory beanFactory;
	private BundleContext bundleContext;
	private List<IComponentCollector> componentCollectors;
	private SpringComponentInfo componentInfo;
	
	public SpringComponentFactoryBean() {
		componentCollectors = new ArrayList<>();
	}
	
	@Override
	public IComponentInfo getObject() throws Exception {
		return componentInfo;
	}

	@Override
	public Class<IComponentInfo> getObjectType() {
		return IComponentInfo.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	public void setComponentId(String name) {
		this.componentId = name;
	}
	
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	@Override
	public synchronized void afterPropertiesSet() throws Exception {
		componentInfo = new SpringComponentInfo(componentId, targetBeanName, beanFactory, bundleContext);
		
		Collection<ServiceReference<IComponentCollector>> srs = bundleContext.getServiceReferences(
				IComponentCollector.class, null);
				 
		if (srs != null) {
			for (ServiceReference<IComponentCollector> sr : srs) {
				IComponentCollector componentCollector = bundleContext.getService(sr);
				componentCollectors.add(componentCollector);
				componentCollector.componentFound(componentInfo);
			}
		}
		
		bundleContext.addServiceListener(this, OsgiFilterUtils.unifyFilter(IComponentCollector.class, null));
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public synchronized void destroy() throws Exception {
		if (!componentCollectors.isEmpty()) {
			for (IComponentCollector componentCollector : componentCollectors) {
				componentCollector.componentLost(componentId);				
			}
		}
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public synchronized void serviceChanged(ServiceEvent event) {
		ServiceReference<?> sr = event.getServiceReference();
		IComponentCollector componentCollector = (IComponentCollector)bundleContext.getService(sr);
		
		if (event.getType() == ServiceEvent.REGISTERED) {
			componentCollectors.add(componentCollector);
			componentCollector.componentFound(componentInfo);
		} else if (event.getType() == ServiceEvent.UNREGISTERING) {
			componentCollectors.remove(componentCollector);
		}
	}

}
